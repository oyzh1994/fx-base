package cn.oyzh.fx.xserver.extension;

import com.github.moaxcp.x11.protocol.XProtocolPlugin;
import com.github.moaxcp.x11.protocol.XRequest;
import com.github.moaxcp.x11.protocol.present.CompleteKind;
import com.github.moaxcp.x11.protocol.present.CompleteMode;
import com.github.moaxcp.x11.protocol.present.CompleteNotifyEvent;
import com.github.moaxcp.x11.protocol.present.Notify;
import com.github.moaxcp.x11.protocol.present.NotifyMSC;
import com.github.moaxcp.x11.protocol.present.Pixmap;
import com.github.moaxcp.x11.protocol.present.PresentPlugin;
import com.github.moaxcp.x11.protocol.present.QueryCapabilities;
import com.github.moaxcp.x11.protocol.present.QueryCapabilitiesReply;
import com.github.moaxcp.x11.protocol.present.QueryVersion;
import com.github.moaxcp.x11.protocol.present.QueryVersionReply;
import com.github.moaxcp.x11.protocol.present.SelectInput;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.client.ClientConnection;
import cn.oyzh.fx.xserver.draw.PixelStore;
import cn.oyzh.fx.xserver.resource.XPixmap;
import cn.oyzh.fx.xserver.resource.XResource;
import cn.oyzh.fx.xserver.resource.XWindow;

import java.awt.Rectangle;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Present: a client hands the server a finished frame and hears when it has been shown.
 *
 * <p>This is the path a browser, a video player or a GL client takes when it has rendered a frame
 * itself and wants it on screen - the same shape as the software GL client B6 measured, except the
 * frame arrives as one {@code PresentPixmap} instead of a burst of {@code PutImage}. What the server
 * does with it here is what the store already knows how to do: the pixmap is copied into the window's
 * own pixels and composited from there, so a redirected window's frame lands in the pixmap a
 * compositor reads and nowhere else (Composite), with no second drawing path to keep in step.
 *
 * <p><b>Measured against a reference, and one rule here is the opposite of the other two extensions'
 * </b> ({@code tools/PresentProbe}, Xvfb): {@code QueryVersion} **echoes the client's own version**
 * - 1.2 to a client asking for 1.2 and 1.0 to one asking for 1.0 - where Composite and SYNC both
 * answer the server's. The obvious implementation is right here and wrong there, which is exactly
 * why it is measured each time rather than carried over. {@code QueryCapabilities} answers **no**
 * capabilities, which is the reference's answer too: this server has no vblank, so it cannot promise
 * the asynchronous or fence-driven paths those bits describe.
 *
 * <p><b>The frame arrives, and the first measurement of that was wrong in a way worth keeping.</b>
 * Read naively - the pixels first, then the event - the reference appears *not* to copy the pixmap
 * into the window, and this entry was written saying so, with the difference recorded as deliberate.
 * Read in the other order it does: the present is asynchronous there, so the copy completes by the
 * time the completion notification is delivered, and asking for the pixels before reading that
 * notification samples a race. {@code tools/PresentProbe} now reads the event first, which is the
 * order that works against both servers, and the two runs are **identical**. There is no difference
 * here to document.
 *
 * <p><b>What is not implemented.</b> {@code target-msc}, {@code divisor} and {@code remainder} are
 * accepted and ignored, because they exist to line a frame up with a vertical blank and there is no
 * blank here to line up with; the completion is reported as {@code Copy}, which is what happened.
 * Sync fences are honoured in the sense that they are accepted - they are already triggered, since
 * this server finishes every request before it reads the next (SYNC).
 */
public final class PresentExtension implements Extension {

    /** The minor opcode of {@code PresentPixmap}. */
    private static final int PIXMAP = 1;
    /** Its fixed part in bytes: everything before the trailing {@code notifies} list. */
    private static final int FIXED_BYTES = 72;

    private final PresentPlugin plugin = new PresentPlugin();

    /** What each client asked to hear about, per window it subscribed with. */
    private final Map<ClientConnection, Map<Integer, Integer>> subscriptions =
            new ConcurrentHashMap<>();

    @Override
    public String name() {
        return "Present";
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

    /**
     * {@code PresentPixmap} is decoded here rather than by the generated reader, for the reason
     * {@code VariableLengthDecoders} documents for sixteen core requests and XFixes' two: the
     * generated reader sizes its trailing {@code notifies} list with {@code withInitialCapacity(
     * length - javaStart)}, where {@code length} is in four byte units and {@code javaStart} is a byte
     * offset. For this request the fixed part is 72 bytes, so a present with no notifies - which is
     * every present a client that wants nothing extra sends, and what this probe sends - asks for a
     * capacity of {@code 4*units - <start>} negative and the whole request is refused:
     * `initial capacity cannot be less than 0`, measured against this server before the decoder
     * existed.
     *
     * <p>The field order and types are the generated reader's, which are right; only the arithmetic
     * is replaced, and the count comes from the request's own length field the way every hand-written
     * decoder here does it.
     */
    @Override
    public XRequest decodeRequest(int minorOpcode, com.github.moaxcp.x11.protocol.X11Input in)
            throws java.io.IOException {
        if ((minorOpcode & 0xFF) != PIXMAP) {
            return null;
        }
        in.readPad(1);                                  // the minor opcode, already known
        int lengthUnits = Short.toUnsignedInt(in.readCard16());
        int window = in.readCard32();
        int pixmap = in.readCard32();
        int serial = in.readCard32();
        int valid = in.readCard32();
        int update = in.readCard32();
        short xOff = in.readInt16();
        short yOff = in.readInt16();
        int targetCrtc = in.readCard32();
        int waitFence = in.readCard32();
        int idleFence = in.readCard32();
        int options = in.readCard32();
        in.readPad(4);
        long targetMsc = in.readCard64();
        long divisor = in.readCard64();
        long remainder = in.readCard64();
        int count = Math.max(lengthUnits * 4 - FIXED_BYTES, 0) / 8;
        org.eclipse.collections.api.list.MutableList<Notify> notifies =
                org.eclipse.collections.impl.factory.Lists.mutable.withInitialCapacity(count);
        for (int i = 0; i < count; i++) {
            notifies.add(Notify.readNotify(in));
        }
        in.readPadAlign(4);
        return Pixmap.builder()
                .window(window).pixmap(pixmap).serial(serial).valid(valid).update(update)
                .xOff(xOff).yOff(yOff).targetCrtc(targetCrtc)
                .waitFence(waitFence).idleFence(idleFence).options(options)
                .targetMsc(targetMsc).divisor(divisor).remainder(remainder)
                .notifies(notifies.toImmutable())
                .build();
    }

    @Override
    public void handleRequest(XServer server, ClientConnection client, XRequest request) {
        if (request instanceof QueryVersion r) {
            // Measured: the reference echoes the client's version here. Composite and SYNC answer
            // their own, so this is a per-extension rule and not a house style.
            client.sendReply(QueryVersionReply.builder()
                    .sequenceNumber(client.sequenceNumber())
                    .majorVersion(r.getMajorVersion())
                    .minorVersion(r.getMinorVersion())
                    .build());
        } else if (request instanceof QueryCapabilities) {
            client.sendReply(QueryCapabilitiesReply.builder()
                    .sequenceNumber(client.sequenceNumber())
                    .capabilities(0)                    // the reference's answer, and the honest one
                    .build());
        } else if (request instanceof SelectInput r) {
            subscriptions.computeIfAbsent(client, c -> new ConcurrentHashMap<>())
                    .put(r.getWindow(), r.getEventMask());
        } else if (request instanceof Pixmap r) {
            presentPixmap(server, client, r);
        } else if (request instanceof NotifyMSC r) {
            notifyMsc(server, client, r);
        } else {
            throw new IllegalArgumentException("unhandled Present request " + request.getClass());
        }
    }

    @Override
    public void clientDisconnected(XServer server, ClientConnection client) {
        subscriptions.remove(client);
    }

    // ------------------------------------------------------------------

    /**
     * Copies the pixmap into the window and reports the completion.
     *
     * <p>Straight into the window's own pixels when it keeps them - which is every window but the
     * root - and onto the screen when it does not, which is the same split every drawing request
     * makes ({@code DrawHandlers.target}).
     */
    private void presentPixmap(XServer server, ClientConnection client, Pixmap request) {
        XWindow window = (XWindow) server.resources().require(client, request.getWindow(),
                XResource.CLASS_WINDOW, false);
        XPixmap source = (XPixmap) server.resources().require(client, request.getPixmap(),
                XResource.CLASS_PIXMAP, false);
        int xOff = request.getXOff();
        int yOff = request.getYOff();
        int width = source.width();
        int height = source.height();
        if (window.keepsOwnPixels()) {
            PixelStore target = window.ownPixels();
            target.copyFrom(source, 0, 0, xOff, yOff, width, height);
            server.compositeOwnPixels(window, new Rectangle(xOff, yOff, width, height));
        } else {
            int x = window.absoluteX() + xOff;
            int y = window.absoluteY() + yOff;
            PixelStore target = window.screen().framebuffer();
            target.copyFrom(source, 0, 0, x, y, width, height);
            server.markScreenDirty(window.screen(), new Rectangle(x, y, width, height));
        }
        complete(server, client, window, request.getSerial(), CompleteKind.PIXMAP,
                CompleteMode.COPY);
    }

    private void notifyMsc(XServer server, ClientConnection client, NotifyMSC request) {
        XWindow window = (XWindow) server.resources().require(client, request.getWindow(),
                XResource.CLASS_WINDOW, false);
        complete(server, client, window, request.getSerial(), CompleteKind.NOTIFY_M_S_C,
                CompleteMode.COPY);
    }

    /**
     * The completion notification, to every client watching that window.
     *
     * <p>{@code msc} and {@code ust} are the server's clock rather than a vertical blank count: this
     * server has no blank to count, and a number a client can use as a monotonic timestamp is more
     * honest than a fabricated frame counter. The reference reports its own uptime here, so the two
     * differ by construction - which is why the probe compares the event's kind, mode and serial and
     * not its clock.
     */
    private void complete(XServer server, ClientConnection client, XWindow window, int serial,
                          CompleteKind kind, CompleteMode mode) {
        long now = server.events().time();
        for (Map.Entry<ClientConnection, Map<Integer, Integer>> entry : subscriptions.entrySet()) {
            Integer mask = entry.getValue().get(window.id());
            if (mask == null || (mask & (1 << 1)) == 0) {
                continue;                               // not subscribed to CompleteNotify
            }
            ClientConnection subscriber = entry.getKey();
            server.events().send(subscriber, CompleteNotifyEvent.builder()
                    .firstEventOffset((byte) 0)
                    .sentEvent(false)
                    .extension(majorOpcode())
                    .sequenceNumber(subscriber.sequenceNumber())
                    .eventType((short) 1)               // COMPLETE_NOTIFY
                    .kind((byte) kind.getValue())
                    .mode((byte) mode.getValue())
                    .event(0)
                    .window(window.id())
                    .serial(serial)
                    .ust(now * 1000L)                   // the clock is milliseconds; ust is ns
                    .msc(now)
                    .build());
        }
    }
}
