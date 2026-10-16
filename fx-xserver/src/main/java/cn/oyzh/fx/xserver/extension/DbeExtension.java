package cn.oyzh.fx.xserver.extension;

import com.github.moaxcp.x11.protocol.X11Input;
import com.github.moaxcp.x11.protocol.X11Output;
import com.github.moaxcp.x11.protocol.XError;
import com.github.moaxcp.x11.protocol.XEvent;
import com.github.moaxcp.x11.protocol.XGenericEvent;
import com.github.moaxcp.x11.protocol.XProtocolPlugin;
import com.github.moaxcp.x11.protocol.XReply;
import com.github.moaxcp.x11.protocol.XRequest;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.client.ClientConnection;
import cn.oyzh.fx.xserver.client.ProtocolException;
import cn.oyzh.fx.xserver.display.Screen;
import cn.oyzh.fx.xserver.display.VisualInfo;
import cn.oyzh.fx.xserver.draw.Framebuffer;
import cn.oyzh.fx.xserver.extension.Extension;
import cn.oyzh.fx.xserver.resource.XPixmap;
import cn.oyzh.fx.xserver.resource.XResource;
import cn.oyzh.fx.xserver.resource.XWindow;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * DOUBLE-BUFFER (Xdbe): a client draws into a buffer nobody is looking at, then shows it in one step.
 *
 * <p>Every toolkit's no-flicker path asks for this and every one of them falls back today, which
 * makes it the most reachable of the capability candidates in {@code docs/backlog.md}'s §9. The model
 * is X's own: a back buffer is allocated <em>on</em> a window, it is a <b>drawable</b> - the ordinary
 * requests address it by its id - and {@code SwapBuffers} makes what was drawn into it the window's
 * contents.
 *
 * <p><b>Everything here was measured with {@code tools/DbeProbe} against a reference server</b>,
 * because three of the four things a first implementation gets wrong are not in the specification's
 * own tables. They are, in the order they bite:
 *
 * <ul>
 *   <li>{@code AllocateBackBufferName} is <b>window, buffer, swapAction</b> - the buffer before the
 *       action - and it has <b>no reply</b> at all. A client learns which window its buffer belongs
 *       to from {@code GetBackBufferAttributes}, which is how Xlib's own
 *       {@code XdbeAllocateBackBufferName} behaves (it returns {@code Success} without reading
 *       anything).</li>
 *   <li>{@code SwapBuffers} names a <b>window</b>, not a buffer: the request is a count and that many
 *       (window, swapAction) pairs, and a window's buffers are swapped together. There is no field in
 *       it that could name a buffer.</li>
 *   <li>{@code GetBackBufferAttributes} is minor opcode <b>7</b> - 6 is {@code GetVisualInfo}, and a
 *       client that sends 6 with a buffer in it is answered {@code BadLength} with the buffer as the
 *       error's value, which reads as "the buffer is bad" rather than "the request is".</li>
 *   <li>{@code GetVisualInfo}'s reply is <b>not</b> the flat list its header comment describes: it is
 *       a count of the drawables answered, and then, <em>per drawable</em>, a count followed by that
 *       many {@code {visualID, depth, perfLevel}} records. The measured reply for one drawable on the
 *       reference is 289 units long with 144 records in it, where a flat reading expects two records
 *       in eight bytes - and the arithmetic of the two readings differs, which is what settled it.</li>
 * </ul>
 *
 * <p><b>What each swap action does</b> is the other half that had to be measured, and one rule
 * explains all four: the front takes the <em>first allocated</em> buffer's contents, and then every
 * buffer of that window is left holding what the action names - {@code Copied} and {@code Undefined}
 * the new front (the reference treats the two identically), {@code Untouched} the front's previous
 * contents, {@code Background} the window's background. Measured colour by colour with two buffers on
 * one window, which is what separates "the buffer keeps its own contents" from "the buffer is given
 * the front's": for a lone buffer the two are indistinguishable, and for two they are not.
 *
 * <p><b>The refusals are as measured</b>: a swap for a window with no buffers is {@code BadMatch} (a
 * client that forgot to allocate gets told so), an undefined action is {@code BadValue} on both the
 * allocate and the swap, an id that is not the client's own buffer is {@code BadBuffer} when
 * deallocated and answered with a <b>zero attribute</b> when queried - the reference answers
 * {@code None} rather than raising, and a client that checks first is better served by an answer it
 * can read. A <em>second client</em> can swap another client's window (measured: the reference allows
 * it - the request names a window, and windows are not private in X), but cannot see or free another
 * client's buffer.
 *
 * <p><b>A buffer follows the window it is on</b>: a resize reallocates it to the window's new size and
 * fills it with the window's background, which is what a reference was measured doing (a buffer read
 * outside its old size answers the background, not an error). That is what
 * {@link Extension#windowResized} is here for - the resize happens in the core {@code ConfigureWindow}
 * path, and an extension holding buffers sized to a window cannot wait to be asked.
 *
 * <p>The back buffer itself is an ordinary {@link XPixmap} registered under the client's id, which is
 * what makes it drawable by every core request for free - and what the reference does internally too.
 * A buffer is created at the window's size and depth, and its initial contents are left as the
 * specification leaves them (undefined): a client that swaps before drawing gets a zeroed buffer, and
 * the reference gives whatever its memory held.
 */
public final class DbeExtension implements Extension {

    /** The extension's requests, in the protocol's own numbering. */
    private static final int GET_VERSION = 0;
    private static final int ALLOCATE_BACK_BUFFER = 1;
    private static final int DEALLOCATE_BACK_BUFFER = 2;
    private static final int SWAP_BUFFERS = 3;
    private static final int BEGIN_IDIOM = 4;
    private static final int END_IDIOM = 5;
    private static final int GET_VISUAL_INFO = 6;
    private static final int GET_BACK_BUFFER_ATTRIBUTES = 7;

    /** {@code SwapAction}, in the protocol's order. */
    private static final int UNDEFINED = 0;
    private static final int BACKGROUND = 1;
    private static final int UNTOUCHED = 2;
    private static final int COPIED = 3;

    /** Measured: a reference answers **1.0** to every client, whatever version it asked with. */
    private static final int MAJOR_VERSION = 1;
    private static final int MINOR_VERSION = 0;

    /** This extension's one error, at {@code firstError + 0}. */
    private static final int BAD_BUFFER = 0;

    private final DbePlugin plugin = new DbePlugin();

    /**
     * The buffers that exist, by the id a client allocated them under, in allocation order.
     *
     * <p>The order is the whole reason this is a {@link LinkedHashMap}: a swap shows the <em>first</em>
     * buffer a window was given, which was measured with two buffers and is not something a client can
     * tell from the protocol. Every buffer here belongs to the client that allocated it - the map is
     * consulted per request against that client - and entries are dropped when the buffer resource is
     * freed, whether by {@code DeallocateBackBufferName} or by the client going away.
     */
    private final Map<Integer, Buffer> buffers = new LinkedHashMap<>();

    private XServer server;

    @Override
    public void init(XServer server) {
        this.server = server;
    }

    @Override
    public String name() {
        return "DOUBLE-BUFFER";
    }

    @Override
    public byte majorOpcode() {
        return plugin.getMajorOpcode();
    }

    @Override
    public byte firstEvent() {
        return plugin.getFirstEvent();
    }

    @Override
    public byte firstError() {
        return plugin.getFirstError();
    }

    @Override
    public XProtocolPlugin plugin() {
        return plugin;
    }

    /** One back buffer: the window it was allocated on and the pixmap that holds its pixels. */
    private record Buffer(XWindow window, XPixmap pixmap) {
    }

    // ------------------------------------------------------------------
    // Decoding
    // ------------------------------------------------------------------

    /**
     * Every DBE request, decoded here: the library generates no module for this extension, and the
     * layouts above are the measured ones rather than the specification's tables.
     *
     * <p>The four header bytes are consumed the way every generated reader consumes them - the minor
     * opcode and the length, both already known to the dispatcher - so the stream is left exactly
     * where the next request begins.
     */
    @Override
    public XRequest decodeRequest(int minorOpcode, X11Input in) throws IOException {
        int minor = minorOpcode & 0xFF;
        if (minor == GET_VERSION) {
            in.readPad(1);
            in.readCard16();
            int clientMajor = in.readCard8();
            int clientMinor = in.readCard8();
            in.readPadAlign(8);
            return new GetVersion(clientMajor, clientMinor);
        }
        if (minor == ALLOCATE_BACK_BUFFER) {
            in.readPad(1);
            in.readCard16();
            int window = in.readCard32();
            int buffer = in.readCard32();
            int swapAction = in.readCard8();
            in.readPadAlign(16);
            return new AllocateBackBuffer(window, buffer, swapAction);
        }
        if (minor == DEALLOCATE_BACK_BUFFER) {
            in.readPad(1);
            in.readCard16();
            int buffer = in.readCard32();
            in.readPadAlign(8);
            return new DeallocateBackBuffer(buffer);
        }
        if (minor == SWAP_BUFFERS) {
            in.readPad(1);
            in.readCard16();
            int count = in.readCard32();
            List<SwapInfo> swaps = new ArrayList<>(Math.max(0, count));
            for (int i = 0; i < count; i++) {
                int window = in.readCard32();
                int swapAction = in.readCard8();
                in.readPad(3);
                swaps.add(new SwapInfo(window, swapAction));
            }
            return new SwapBuffers(swaps);
        }
        if (minor == BEGIN_IDIOM || minor == END_IDIOM) {
            in.readPad(1);
            in.readCard16();
            return new Idiom(minor);
        }
        if (minor == GET_VISUAL_INFO) {
            in.readPad(1);
            in.readCard16();
            int count = in.readCard32();
            List<Integer> drawables = new ArrayList<>(Math.max(0, count));
            for (int i = 0; i < count; i++) {
                drawables.add(in.readCard32());
            }
            return new GetVisualInfo(drawables);
        }
        if (minor == GET_BACK_BUFFER_ATTRIBUTES) {
            in.readPad(1);
            in.readCard16();
            int buffer = in.readCard32();
            in.readPadAlign(8);
            return new GetBackBufferAttributes(buffer);
        }
        // A minor opcode this extension does not have. A reference answers BadRequest for one, and a
        // client that is told nothing would go on believing its request was served.
        throw new ProtocolException(ProtocolException.REQUEST, minor,
                "unknown DOUBLE-BUFFER request " + minor);
    }

    // ------------------------------------------------------------------
    // Behaviour
    // ------------------------------------------------------------------

    @Override
    public void handleRequest(XServer server, ClientConnection client, XRequest request) {
        if (request instanceof GetVersion version) {
            handleGetVersion(client, version);
            return;
        }
        if (request instanceof AllocateBackBuffer allocate) {
            handleAllocate(server, client, allocate);
            return;
        }
        if (request instanceof DeallocateBackBuffer deallocate) {
            handleDeallocate(client, deallocate);
            return;
        }
        if (request instanceof SwapBuffers swap) {
            handleSwap(server, client, swap);
            return;
        }
        if (request instanceof Idiom) {
            // BeginIdiom and EndIdiom are hints that the swaps between them belong together: a
            // reference accepts them, nested or not, and a probe cannot tell they were sent.
            return;
        }
        if (request instanceof GetVisualInfo visuals) {
            handleGetVisualInfo(server, client, visuals);
            return;
        }
        if (request instanceof GetBackBufferAttributes attributes) {
            handleGetAttributes(server, client, attributes);
            return;
        }
        throw new IllegalArgumentException("unhandled DOUBLE-BUFFER request " + request.getClass());
    }

    /** The server's own version, to every client that asks, whatever it asked for. */
    private void handleGetVersion(ClientConnection client, GetVersion request) {
        client.sendReply(new VersionReply(client.sequenceNumber(), MAJOR_VERSION, MINOR_VERSION));
    }

    /**
     * Allocates a back buffer on a window: a pixmap of the window's size, registered under the
     * client's own id so that every core request can draw into it, and remembered as this window's
     * first (or next) buffer. No reply - a client learns the window from
     * {@code GetBackBufferAttributes}.
     */
    private void handleAllocate(XServer server, ClientConnection client, AllocateBackBuffer request) {
        XResource resource = server.resources().get(request.window());
        if (!(resource instanceof XWindow window) || window.isDestroyed()) {
            throw ProtocolException.badWindow(request.window());
        }
        if (request.swapAction() > COPIED) {
            // The reference answers BadValue with the *window* as the error's value, which is the
            // field a client can act on: it names the request that carried the bad action.
            throw ProtocolException.badValue(request.window());
        }
        if (!client.ownsIdRange(request.buffer()) || server.resources().contains(request.buffer())) {
            // The id is the client's to choose and must be its own and free: an id already in use is
            // BadIDChoice rather than BadBuffer, which is what the reference answers for a second
            // allocate of a buffer id that is already one (measured) - and one outside the client's
            // range is the same error, as it is for every other resource a client creates.
            throw ProtocolException.badIdChoice(request.buffer());
        }
        XPixmap pixmap = new XPixmap(request.buffer(), client, window.screen(),
                window.width(), window.height(), window.depth(), window.visual());
        server.resources().add(pixmap);
        synchronized (this) {
            buffers.put(request.buffer(), new Buffer(window, pixmap));
        }
    }

    /**
     * Frees a back buffer. Only the client that allocated it may: for anyone else the id is not a
     * buffer of theirs, which is {@code BadBuffer} - this extension's own error, raised for an id
     * that is not a buffer of the requesting client, whether it never was one or belongs to somebody
     * else.
     */
    private void handleDeallocate(ClientConnection client, DeallocateBackBuffer request) {
        synchronized (this) {
            Buffer buffer = buffers.get(request.buffer());
            if (buffer == null || buffer.pixmap().owner() != client) {
                throw badBuffer(request.buffer());
            }
            buffers.remove(request.buffer());
            server.resources().free(request.buffer());
        }
    }

    /**
     * Swaps a window's buffers in front of its pixels, one pair at a time.
     *
     * <p>The whole request is validated before any of it happens, which is measured:
     * {@code SwapBuffers} with two pairs, the second on a window that has no buffers, left the first
     * window's pixels exactly as they were on the reference.
     */
    private void handleSwap(XServer server, ClientConnection client, SwapBuffers request) {
        List<XWindow> windows = new ArrayList<>(request.swaps().size());
        for (SwapInfo swap : request.swaps()) {
            XResource resource = server.resources().get(swap.window());
            if (!(resource instanceof XWindow window) || window.isDestroyed()) {
                throw ProtocolException.badWindow(swap.window());
            }
            if (swap.swapAction() > COPIED) {
                throw ProtocolException.badValue(swap.window());
            }
            if (liveBuffers(window).isEmpty()) {
                throw ProtocolException.badMatch();
            }
            windows.add(window);
        }
        for (int i = 0; i < windows.size(); i++) {
            swap(server, windows.get(i), request.swaps().get(i).swapAction());
        }
    }

    /**
     * One swap: the front takes the first buffer's contents, and then every buffer of that window is
     * left holding what the action names.
     *
     * <p>Written as two copies of the whole surface rather than by exchanging pixel arrays, because
     * the window's store is shared with the compositor, the damage path and {@code NameWindowPixmap}'s
     * view of it: replacing the array would leave every one of them reading the old pixels.
     */
    private void swap(XServer server, XWindow window, int action) {
        List<Buffer> live = liveBuffers(window);
        if (live.isEmpty()) {
            return;
        }
        XPixmap front = live.get(0).pixmap();
        int width = window.width();
        int height = window.height();
        synchronized (this) {
            Framebuffer store = window.ownPixels();
            // The old front is needed only by Untouched, and only across this swap.
            int[] previous = action == UNTOUCHED ? snapshot(store, width, height) : null;
            store.copyFrom(front, 0, 0, 0, 0, Math.min(width, front.width()),
                    Math.min(height, front.height()));
            for (Buffer buffer : live) {
                XPixmap pixmap = buffer.pixmap();
                if (action == UNTOUCHED) {
                    restore(pixmap, previous, width, height);
                } else if (action == BACKGROUND) {
                    pixmap.fillRect(0, 0, pixmap.width(), pixmap.height(),
                            window.backgroundPixel());
                } else {
                    // Copied and Undefined: the reference treats the undefined action as a copy, and
                    // the two are indistinguishable here for the same reason they are there - the
                    // buffer is handed to the front and then holds what the front holds.
                    pixmap.copyFrom(store, 0, 0, 0, 0, Math.min(pixmap.width(), width),
                            Math.min(pixmap.height(), height));
                }
            }
        }
        // What the client drew is now the window's contents: composite it, and let the frame that
        // follows present one whole window rather than a buffer's worth of it.
        server.compositeOwnPixels(window, new Rectangle(0, 0, width, height));
    }

    /**
     * {@code GetVisualInfo}: which visuals of the drawables' screens support double buffering, and
     * what a swap costs on each.
     *
     * <p>The reply is the measured shape - a count of sections, then per section a count and that
     * many eight byte records - and not the flat list the protocol header's comment describes. A
     * section is one **drawable the client named** and its screen's visuals: a reference answers with
     * every visual of that screen (144 of them, and 289 units of reply for one drawable, which is
     * where the flat reading stops adding up), and **for a request that names no drawable at all it
     * answers one section per screen of the display** - measured, and not an empty reply.
     */
    private void handleGetVisualInfo(XServer server, ClientConnection client,
                                     GetVisualInfo request) {
        if (request.drawables().isEmpty()) {
            // A client that names no drawable is asking about the display: a reference answers one
            // section per **screen**, with that screen's visuals - measured, and the reason this
            // request's count of sections is not simply the count of drawables it carried.
            client.sendReply(new VisualInfoReply(client.sequenceNumber(), server.screens().size(),
                    sections(server.screens(), client.isBigEndian())));
            return;
        }
        List<Screen> screens = new ArrayList<>(request.drawables().size());
        for (int id : request.drawables()) {
            XResource resource = server.resources().get(id);
            Screen screen = resource instanceof XWindow window ? window.screen()
                    : resource instanceof XPixmap pixmap ? pixmap.screen() : null;
            if (screen == null || resource.isDestroyed()) {
                throw ProtocolException.badDrawable(id);
            }
            screens.add(screen);
        }
        client.sendReply(new VisualInfoReply(client.sequenceNumber(), screens.size(),
                sections(screens, client.isBigEndian())));
    }

    /**
     * The variable part of a {@code GetVisualInfo} reply: one section per screen, each a count of
     * visuals followed by that many eight byte records.
     */
    private static byte[] sections(List<Screen> screens, boolean bigEndian) {
        int size = 0;
        for (Screen screen : screens) {
            size += 4 + screen.visuals().size() * 8;
        }
        byte[] data = new byte[size];
        int at = 0;
        for (Screen screen : screens) {
            List<VisualInfo> visuals = screen.visuals();
            putCard32(data, at, visuals.size(), bigEndian);
            at += 4;
            for (VisualInfo visual : visuals) {
                putCard32(data, at, visual.id(), bigEndian);
                data[at + 4] = (byte) visual.depth();
                // The performance level is a hint about what a swap costs - lower is better - and a
                // server with no hardware behind it has nothing to say, which is what the reference
                // answers for every visual too (0).
                data[at + 5] = 0;
                at += 8;
            }
        }
        return data;
    }

    /**
     * {@code GetBackBufferAttributes}: the window a buffer belongs to, or <b>zero</b> when the id is
     * not a buffer of the asking client - which is what a reference answers, for an id that was never
     * a buffer and for one that belongs to somebody else. It does not raise: a client asking about an
     * id it has not allocated is asking a question, and an answer it can read is more use than an
     * error it has to handle.
     */
    private void handleGetAttributes(XServer server, ClientConnection client,
                                     GetBackBufferAttributes request) {
        int window = 0;
        synchronized (this) {
            Buffer buffer = buffers.get(request.buffer());
            if (buffer != null && buffer.pixmap().owner() == client) {
                window = buffer.window().id();
            }
        }
        client.sendReply(new AttributesReply(client.sequenceNumber(), window));
    }

    /**
     * A resize reallocates a window's buffers and fills them with its background.
     *
     * <p>Measured: after the window grew, a reference's buffer follows the new size and reads as the
     * window's background - not as an error outside its old bounds and not as what it held before.
     * The pixmap is replaced rather than resized, because a resource's pixels and size are fixed at
     * allocation; the id the client holds is the same, and every request resolves it again.
     */
    @Override
    public void windowResized(XServer server, XWindow window, int oldWidth, int oldHeight) {
        if (window.width() == oldWidth && window.height() == oldHeight) {
            return;
        }
        synchronized (this) {
            for (Buffer buffer : liveBuffers(window)) {
                int id = buffer.pixmap().id();
                server.resources().free(id);
                XPixmap resized = new XPixmap(id, buffer.pixmap().owner(), window.screen(),
                        window.width(), window.height(), window.depth(), window.visual());
                resized.fillRect(0, 0, resized.width(), resized.height(),
                        window.backgroundPixel());
                server.resources().add(resized);
                buffers.put(id, new Buffer(window, resized));
            }
        }
    }

    /** Lets go of a departing client's buffers; the resource table frees the pixels themselves. */
    @Override
    public void clientDisconnected(XServer server, ClientConnection client) {
        synchronized (this) {
            buffers.values().removeIf(buffer -> buffer.pixmap().owner() == client);
        }
    }

    // ------------------------------------------------------------------
    // The buffer table
    // ------------------------------------------------------------------

    /**
     * The buffers of a window that still exist, in allocation order.
     *
     * <p>Pruned on the way through: a buffer's pixmap can be freed by a core {@code FreePixmap} rather
     * than by {@code DeallocateBackBufferName}, and a window can be destroyed with buffers on it, so
     * "what this extension remembers" is checked against the resource table instead of trusted.
     */
    private synchronized List<Buffer> liveBuffers(XWindow window) {
        List<Buffer> live = new ArrayList<>(2);
        buffers.values().removeIf(buffer -> {
            boolean gone = buffer.pixmap().isDestroyed() || buffer.window().isDestroyed();
            if (!gone && buffer.window().id() == window.id()) {
                live.add(buffer);
            }
            return gone;
        });
        return live;
    }

    /** The copy of a surface this server takes to remember what the front held before a swap. */
    private static int[] snapshot(Framebuffer store, int width, int height) {
        int[] copy = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                copy[y * width + x] = store.pixel(x, y);
            }
        }
        return copy;
    }

    private static void restore(XPixmap pixmap, int[] pixels, int width, int height) {
        for (int y = 0; y < Math.min(height, pixmap.height()); y++) {
            for (int x = 0; x < Math.min(width, pixmap.width()); x++) {
                pixmap.setPixel(x, y, pixels[y * width + x]);
            }
        }
    }

    private ProtocolException badBuffer(int id) {
        return new ProtocolException((byte) (firstError() + BAD_BUFFER), id,
                "bad back buffer 0x" + Integer.toHexString(id));
    }

    private static void putCard16(byte[] bytes, int offset, int value, boolean bigEndian) {
        if (bigEndian) {
            bytes[offset] = (byte) (value >> 8);
            bytes[offset + 1] = (byte) value;
        } else {
            bytes[offset] = (byte) value;
            bytes[offset + 1] = (byte) (value >> 8);
        }
    }

    private static void putCard32(byte[] bytes, int offset, int value, boolean bigEndian) {
        if (bigEndian) {
            bytes[offset] = (byte) (value >> 24);
            bytes[offset + 1] = (byte) (value >> 16);
            bytes[offset + 2] = (byte) (value >> 8);
            bytes[offset + 3] = (byte) value;
        } else {
            bytes[offset] = (byte) value;
            bytes[offset + 1] = (byte) (value >> 8);
            bytes[offset + 2] = (byte) (value >> 16);
            bytes[offset + 3] = (byte) (value >> 24);
        }
    }

    // ------------------------------------------------------------------
    // The requests, as objects
    // ------------------------------------------------------------------

    /** {@code QueryVersion}: two version bytes, sent by a client that is checking for DBE. */
    private record GetVersion(int clientMajor, int clientMinor) implements XRequest {

        @Override
        public byte getOpCode() {
            return (byte) GET_VERSION;
        }

        @Override
        public int getSize() {
            return 8;
        }

        @Override
        public String getPluginName() {
            return "dbe";
        }

        @Override
        public void write(byte majorOpcode, X11Output out) throws IOException {
            out.writeCard8(majorOpcode);
            out.writeCard8((byte) GET_VERSION);
            out.writeCard16((short) 2);
            out.writeCard8((byte) clientMajor);
            out.writeCard8((byte) clientMinor);
            out.writePadAlign(8);
        }
    }

    /** {@code AllocateBackBufferName}: a window, the buffer's id and a swap action. No reply. */
    private record AllocateBackBuffer(int window, int buffer, int swapAction) implements XRequest {

        @Override
        public byte getOpCode() {
            return (byte) ALLOCATE_BACK_BUFFER;
        }

        @Override
        public int getSize() {
            return 16;
        }

        @Override
        public String getPluginName() {
            return "dbe";
        }

        @Override
        public void write(byte majorOpcode, X11Output out) throws IOException {
            out.writeCard8(majorOpcode);
            out.writeCard8((byte) ALLOCATE_BACK_BUFFER);
            out.writeCard16((short) 4);
            out.writeCard32(window);
            out.writeCard32(buffer);
            out.writeCard8((byte) swapAction);
            out.writePadAlign(16);
        }
    }

    /** {@code DeallocateBackBufferName}: the buffer's id. */
    private record DeallocateBackBuffer(int buffer) implements XRequest {

        @Override
        public byte getOpCode() {
            return (byte) DEALLOCATE_BACK_BUFFER;
        }

        @Override
        public int getSize() {
            return 8;
        }

        @Override
        public String getPluginName() {
            return "dbe";
        }

        @Override
        public void write(byte majorOpcode, X11Output out) throws IOException {
            out.writeCard8(majorOpcode);
            out.writeCard8((byte) DEALLOCATE_BACK_BUFFER);
            out.writeCard16((short) 2);
            out.writeCard32(buffer);
        }
    }

    /** One (window, swap action) pair of a {@code SwapBuffers} request. */
    private record SwapInfo(int window, int swapAction) {
    }

    /** {@code SwapBuffers}: a count and that many (window, swap action) pairs. */
    private record SwapBuffers(List<SwapInfo> swaps) implements XRequest {

        @Override
        public byte getOpCode() {
            return (byte) SWAP_BUFFERS;
        }

        @Override
        public int getSize() {
            return 8 + swaps.size() * 8;
        }

        @Override
        public String getPluginName() {
            return "dbe";
        }

        @Override
        public void write(byte majorOpcode, X11Output out) throws IOException {
            out.writeCard8(majorOpcode);
            out.writeCard8((byte) SWAP_BUFFERS);
            out.writeCard16((short) (2 + swaps.size() * 2));
            out.writeCard32(swaps.size());
            for (SwapInfo swap : swaps) {
                out.writeCard32(swap.window());
                out.writeCard8((byte) swap.swapAction());
                out.writePad(3);
            }
        }
    }

    /** {@code BeginIdiom} or {@code EndIdiom}: a hint, and nothing to carry. */
    private record Idiom(int minorOpcode) implements XRequest {

        @Override
        public byte getOpCode() {
            return (byte) minorOpcode;
        }

        @Override
        public int getSize() {
            return 4;
        }

        @Override
        public String getPluginName() {
            return "dbe";
        }

        @Override
        public void write(byte majorOpcode, X11Output out) throws IOException {
            out.writeCard8(majorOpcode);
            out.writeCard8((byte) minorOpcode);
            out.writeCard16((short) 1);
        }
    }

    /** {@code GetVisualInfo}: the drawables to describe. */
    private record GetVisualInfo(List<Integer> drawables) implements XRequest {

        @Override
        public byte getOpCode() {
            return (byte) GET_VISUAL_INFO;
        }

        @Override
        public int getSize() {
            return 8 + drawables.size() * 4;
        }

        @Override
        public String getPluginName() {
            return "dbe";
        }

        @Override
        public void write(byte majorOpcode, X11Output out) throws IOException {
            out.writeCard8(majorOpcode);
            out.writeCard8((byte) GET_VISUAL_INFO);
            out.writeCard16((short) (2 + drawables.size()));
            out.writeCard32(drawables.size());
            for (int drawable : drawables) {
                out.writeCard32(drawable);
            }
        }
    }

    /** {@code GetBackBufferAttributes}: the buffer to name the window of. */
    private record GetBackBufferAttributes(int buffer) implements XRequest {

        @Override
        public byte getOpCode() {
            return (byte) GET_BACK_BUFFER_ATTRIBUTES;
        }

        @Override
        public int getSize() {
            return 8;
        }

        @Override
        public String getPluginName() {
            return "dbe";
        }

        @Override
        public void write(byte majorOpcode, X11Output out) throws IOException {
            out.writeCard8(majorOpcode);
            out.writeCard8((byte) GET_BACK_BUFFER_ATTRIBUTES);
            out.writeCard16((short) 2);
            out.writeCard32(buffer);
        }
    }

    // ------------------------------------------------------------------
    // The replies
    // ------------------------------------------------------------------

    /** {@code QueryVersion}'s reply: two version bytes at offsets 8 and 9. */
    private record VersionReply(short sequenceNumber, int major, int minor) implements XReply {

        @Override
        public short getSequenceNumber() {
            return sequenceNumber;
        }

        @Override
        public int getSize() {
            return 32;
        }

        @Override
        public String getPluginName() {
            return "dbe";
        }

        @Override
        public void write(X11Output out) throws IOException {
            out.writeCard8(getResponseCode());
            out.writePad(1);
            out.writeCard16(sequenceNumber);
            out.writeCard32(getLength());               // zero: every field fits in the fixed part
            out.writeCard8((byte) major);
            out.writeCard8((byte) minor);
            out.writePad(22);
        }
    }

    /**
     * {@code GetVisualInfo}'s reply: a count of the drawables answered, then per drawable a count and
     * that many records - the measured shape, and not the flat list the protocol header describes.
     *
     * <p>The variable part is built as bytes rather than written field by field because it is a list
     * of lists whose lengths decide their own framing; the length field itself is rewritten from the
     * finished image by {@code ClientOutput}, as it is for every reply.
     */
    private record VisualInfoReply(short sequenceNumber, int drawables, byte[] data)
            implements XReply {

        @Override
        public short getSequenceNumber() {
            return sequenceNumber;
        }

        @Override
        public int getSize() {
            return 32 + data.length;
        }

        @Override
        public String getPluginName() {
            return "dbe";
        }

        @Override
        public void write(X11Output out) throws IOException {
            out.writeCard8(getResponseCode());
            out.writePad(1);
            out.writeCard16(sequenceNumber);
            out.writeCard32(getLength());
            out.writeCard32(drawables);
            out.writePad(20);
            for (byte value : data) {
                out.writeCard8(value);
            }
        }
    }

    /** {@code GetBackBufferAttributes}' reply: one CARD32, the window, or zero. */
    private record AttributesReply(short sequenceNumber, int window) implements XReply {

        @Override
        public short getSequenceNumber() {
            return sequenceNumber;
        }

        @Override
        public int getSize() {
            return 32;
        }

        @Override
        public String getPluginName() {
            return "dbe";
        }

        @Override
        public void write(X11Output out) throws IOException {
            out.writeCard8(getResponseCode());
            out.writePad(1);
            out.writeCard16(sequenceNumber);
            out.writeCard32(getLength());
            out.writeCard32(window);
            out.writePad(20);
        }
    }

    /**
     * The plugin the registry needs for this extension's opcode and error range.
     *
     * <p>It is never asked to decode anything - {@link #decodeRequest} answers every request - so its
     * read methods refuse rather than pretend. It carries the protocol's own names and the error
     * count, which is what {@code QueryExtension} and a client's cache are about: DBE defines one
     * error, {@code BadBuffer}, and no events at all.
     */
    private static final class DbePlugin implements XProtocolPlugin {

        private byte majorOpcode;
        private byte firstEvent;
        private byte firstError;

        @Override
        public String getPluginName() {
            return "dbe";
        }

        @Override
        public Optional<String> getExtensionXName() {
            return Optional.of("DOUBLE-BUFFER");
        }

        @Override
        public Optional<String> getExtensionName() {
            return Optional.of("DoubleBuffer");
        }

        @Override
        public Optional<Boolean> getExtensionMultiword() {
            return Optional.of(true);
        }

        @Override
        public Optional<Byte> getMajorVersion() {
            return Optional.of((byte) MAJOR_VERSION);
        }

        @Override
        public Optional<Byte> getMinorVersion() {
            return Optional.of((byte) MINOR_VERSION);
        }

        @Override
        public byte getMajorOpcode() {
            return majorOpcode;
        }

        @Override
        public void setMajorOpcode(byte majorOpcode) {
            this.majorOpcode = majorOpcode;
        }

        @Override
        public byte getFirstEvent() {
            return firstEvent;
        }

        @Override
        public void setFirstEvent(byte firstEvent) {
            this.firstEvent = firstEvent;
        }

        @Override
        public byte getFirstError() {
            return firstError;
        }

        @Override
        public void setFirstError(byte firstError) {
            this.firstError = firstError;
        }

        @Override
        public boolean supportedRequest(XRequest request) {
            return request instanceof GetVersion || request instanceof AllocateBackBuffer
                    || request instanceof DeallocateBackBuffer || request instanceof SwapBuffers
                    || request instanceof Idiom || request instanceof GetVisualInfo
                    || request instanceof GetBackBufferAttributes;
        }

        @Override
        public boolean supportedRequest(byte majorOpcode, byte minorOpcode) {
            int minor = minorOpcode & 0xFF;
            return minor >= GET_VERSION && minor <= GET_BACK_BUFFER_ATTRIBUTES;
        }

        @Override
        public boolean supportedEvent(byte number) {
            return false;
        }

        @Override
        public boolean supportedError(byte code) {
            return (code & 0xFF) == firstError + BAD_BUFFER;
        }

        @Override
        public <T extends XRequest> T readRequest(byte majorOpcode, byte minorOpcode, X11Input in) {
            throw new IllegalArgumentException("DOUBLE-BUFFER requests are decoded by the extension");
        }

        @Override
        public <T extends XEvent> T readEvent(byte number, boolean sentEvent, X11Input in) {
            throw new IllegalArgumentException("DOUBLE-BUFFER defines no events");
        }

        @Override
        public <T extends XError> T readError(byte code, X11Input in) {
            throw new IllegalArgumentException("DOUBLE-BUFFER errors are written by the server");
        }

        @Override
        public <T extends XGenericEvent> T readGenericEvent(byte firstEventOffset, boolean sentEvent,
                byte extension, short sequenceNumber, int length, short eventType, X11Input in) {
            throw new IllegalArgumentException("DOUBLE-BUFFER defines no events");
        }
    }
}
