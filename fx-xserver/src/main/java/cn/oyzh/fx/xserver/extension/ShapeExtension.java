package cn.oyzh.fx.xserver.extension;

import com.github.moaxcp.x11.protocol.XProtocolPlugin;
import com.github.moaxcp.x11.protocol.XRequest;
import com.github.moaxcp.x11.protocol.shape.Combine;
import com.github.moaxcp.x11.protocol.shape.GetRectangles;
import com.github.moaxcp.x11.protocol.shape.GetRectanglesReply;
import com.github.moaxcp.x11.protocol.shape.InputSelected;
import com.github.moaxcp.x11.protocol.shape.InputSelectedReply;
import com.github.moaxcp.x11.protocol.shape.Mask;
import com.github.moaxcp.x11.protocol.shape.NotifyEvent;
import com.github.moaxcp.x11.protocol.shape.Offset;
import com.github.moaxcp.x11.protocol.shape.QueryExtents;
import com.github.moaxcp.x11.protocol.shape.QueryExtentsReply;
import com.github.moaxcp.x11.protocol.shape.QueryVersion;
import com.github.moaxcp.x11.protocol.shape.QueryVersionReply;
import com.github.moaxcp.x11.protocol.shape.Rectangles;
import com.github.moaxcp.x11.protocol.shape.SelectInput;
import com.github.moaxcp.x11.protocol.shape.ShapePlugin;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.client.ClientConnection;
import cn.oyzh.fx.xserver.client.ProtocolException;
import cn.oyzh.fx.xserver.resource.ShapeState;
import cn.oyzh.fx.xserver.resource.XPixmap;
import cn.oyzh.fx.xserver.resource.XResource;
import cn.oyzh.fx.xserver.resource.XWindow;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * SHAPE.
 *
 * <p>A window is normally a rectangle. SHAPE lets a client replace that rectangle with an
 * arbitrary region, in three flavours: the bounding shape (the region the window occupies, which is
 * what gives a window rounded corners), the clip shape (drawing is clipped to it) and the input
 * shape (the region that accepts pointer events, which is what makes click-through regions work).
 * Every modern toolkit uses it, so a toolkit that finds it missing falls back to square corners.
 *
 * <p>What is enforced today, and what is not:
 * <ul>
 *   <li>The <em>input</em> shape is enforced. {@link #acceptsPointer(XServer, XWindow, int, int)}
 *       answers whether a screen point reaches a window, and {@code InputState} consults it while
 *       walking the window tree, so a pointer over a shaped out corner lands on whatever is below,
 *       exactly as on a real server. No shape means "accept everything", so the check is safe to
 *       call unconditionally.
 *   <li>The <em>bounding</em> and <em>clip</em> shapes are stored, reported through
 *       {@code QueryExtents} and {@code GetRectangles}, used as the fallback input region, and used
 *       when another shape or an XFIXES region is combined with them. Their <em>drawing</em> effect
 *       is not applied: the rasterizer clips to a {@link Rectangle} supplied by its caller and
 *       nothing in the drawing path consults {@link ShapeState}, so a shaped window's corners are
 *       still painted into the framebuffer. Shape changes call {@link XServer#markScreenDirty()} so
 *       the framebuffer is at least repainted, but the corners are not clipped out of it yet.
 * </ul>
 *
 * <p>The state lives in a {@link WeakHashMap} keyed by window because {@code XWindow} has no shape
 * field and cannot be given one. A window that is destroyed is unreachable from the window tree,
 * so its entry is collected rather than leaked.
 */
public final class ShapeExtension implements Extension {

    /** {@code ShapeRectangles}, whose decoder this class has to supply: see {@link #decodeRequest}. */
    private static final int RECTANGLES = 1;

    private static final Logger log = LoggerFactory.getLogger(ShapeExtension.class);

    /** SHAPE 1.1 is what the generated plugin reports, so it is what clients are told. */
    private static final short MAJOR_VERSION = 1;
    private static final short MINOR_VERSION = 1;

    private final ShapePlugin plugin = new ShapePlugin();

    private final Map<XWindow, ShapeState> states = new WeakHashMap<>();
    private final Map<XWindow, Set<ClientConnection>> notifyClients = new WeakHashMap<>();

    /** False until the first shape is set, so the pointer path can skip the map lookup entirely. */
    private volatile boolean anyShapes;

    @Override
    public String name() {
        return "SHAPE";
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

    @Override
    public void handleRequest(XServer server, ClientConnection client, XRequest request) {
        if (request instanceof QueryVersion queryVersion) {
            queryVersion(client);
        } else if (request instanceof Rectangles rectangles) {
            rectangles(server, client, rectangles);
        } else if (request instanceof Mask mask) {
            mask(server, client, mask);
        } else if (request instanceof Combine combine) {
            combine(server, client, combine);
        } else if (request instanceof Offset offset) {
            offset(server, client, offset);
        } else if (request instanceof QueryExtents queryExtents) {
            queryExtents(server, client, queryExtents);
        } else if (request instanceof SelectInput selectInput) {
            selectInput(server, client, selectInput);
        } else if (request instanceof InputSelected inputSelected) {
            inputSelected(server, client, inputSelected);
        } else if (request instanceof GetRectangles getRectangles) {
            getRectangles(server, client, getRectangles);
        } else {
            throw new IllegalArgumentException("unhandled SHAPE request " + request.getClass());
        }
    }

    @Override
    public void clientDisconnected(XServer server, ClientConnection client) {
        synchronized (notifyClients) {
            notifyClients.values().removeIf(clients -> {
                clients.remove(client);
                return clients.isEmpty();
            });
        }
    }

    // ------------------------------------------------------------------
    // Queries used by the rest of the server
    // ------------------------------------------------------------------

    /**
     * The SHAPE state of a window, or null when the window has no shape or SHAPE is not registered.
     * This is how the input path reaches the shape without depending on the extension directly.
     */
    public static ShapeState inputStateFor(XServer server, XWindow window) {
        ShapeExtension shape = of(server);
        return shape == null ? null : shape.stateIfPresent(window);
    }

    /** The SHAPE extension registered with {@code server}, or null if it is not. */
    public static ShapeExtension of(XServer server) {
        Extension extension = server.extensions().byName("SHAPE");
        return extension instanceof ShapeExtension shape ? shape : null;
    }

    /**
     * Whether a screen point reaches {@code window} through its shape. True when the window has no
     * shape and when SHAPE is not registered, so this is safe to call unconditionally wherever the
     * window tree is walked.
     *
     * @param screenX pointer x in screen coordinates
     * @param screenY pointer y in screen coordinates
     */
    public static boolean acceptsPointer(XServer server, XWindow window, int screenX, int screenY) {
        ShapeState state = inputStateFor(server, window);
        return state == null
                || state.contains(screenX - window.absoluteX(), screenY - window.absoluteY());
    }

    /**
     * Hands a window's bounding shape to the window itself, as rectangles in its own coordinates.
     *
     * <p>Nothing to hand over when the shape is unset - which is the common case and the reason the
     * window's field is null there rather than "the whole rectangle": a null is a reference test per
     * request, an all-covering rectangle list is an intersection per request.
     */
    private void pushBoundingShape(XWindow window) {
        ShapeState.Shape shape = stateIfPresent(window) == null
                ? null : state(window).shape(ShapeState.BOUNDING);
        window.setBoundingShape(shape == null || !shape.isSet()
                ? null : shape.absoluteRectangles());
    }

    /** Whether a window relative point reaches the window through its shape. */
    public boolean acceptsWindowPoint(XWindow window, int x, int y) {
        ShapeState state = stateIfPresent(window);
        return state == null || state.contains(x, y);
    }

    /** The state of a window, created on first use. */
    public ShapeState state(XWindow window) {
        anyShapes = true;
        synchronized (states) {
            return states.computeIfAbsent(window, key -> new ShapeState());
        }
    }

    private ShapeState stateIfPresent(XWindow window) {
        if (!anyShapes) {
            return null;
        }
        synchronized (states) {
            return states.get(window);
        }
    }

    /**
     * The shape of a window as rectangles in window coordinates, or the window's own rectangle when
     * it has no shape of that kind. XFIXES uses this to turn a window's shape into a region.
     */
    public List<Rectangle> shapeRectangles(XWindow window, int kind) {
        ShapeState.Shape shape = state(window).shape(kind);
        if (!shape.isSet()) {
            return List.of(new Rectangle(0, 0, window.width(), window.height()));
        }
        return shape.absoluteRectangles();
    }

    /**
     * Replaces one of a window's shapes with a rectangle list. This is how XFIXES'
     * {@code SetWindowShapeRegion} hands its regions over.
     */
    public void setShape(XServer server, XWindow window, int kind, List<Rectangle> rectangles,
                         int xOffset, int yOffset) {
        checkKind(kind);
        apply(server, window, kind, ShapeState.OP_SET,
                ShapeState.Shape.ofRectangles(rectangles, xOffset, yOffset));
    }

    // ------------------------------------------------------------------
    // Requests
    // ------------------------------------------------------------------

    private void queryVersion(ClientConnection client) {
        client.sendReply(QueryVersionReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .majorVersion(MAJOR_VERSION)
                .minorVersion(MINOR_VERSION)
                .build());
    }

    /**
     * {@code ShapeRectangles} is decoded here, and the reason is the one
     * {@link cn.oyzh.fx.xserver.protocol.VariableLengthDecoders} gives for the sixteen core
     * requests it covers: the generated reader sizes its rectangle list with
     * {@code withInitialCapacity(length - javaStart)}, where {@code length} is the request length in
     * *four byte units* and {@code javaStart} a byte offset. For this request the fixed part is
     * sixteen bytes, so any request shorter than sixteen units - **five rectangles or fewer** - gets
     * a negative capacity and is refused with {@code BadRequest} before the handler ever sees it.
     *
     * <p>Measured 2026-09-26 against a live server from the kit's hand-written client: one rectangle
     * is refused ({@code java.lang.IllegalArgumentException: initial capacity cannot be less than 0}
     * in the log), ten are accepted. So a client that shapes a window with a handful of rectangles -
     * the ordinary case, and what {@code XShapeCombineRectangles} sends - was told its request was
     * malformed, and the reason it was written down as "unexplained" in the kit's README (A2) is
     * that the generated readers are right in every respect except this arithmetic.
     *
     * <p>Nothing else is hand-decoded here: {@code Combine}, {@code Mask}, {@code Offset} and the
     * rest carry no trailing list, and the reply readers are the client's side of the wire.
     *
     * <p>One limitation comes with the extension path rather than with this reader: the request's
     * length is the sixteen bit field, which a BIG-REQUEST over 64K cannot describe ({@code
     * RequestDispatcher} writes the capped value into the stream the decoders read). A shape with
     * more than about eight thousand rectangles would therefore be read short. The core requests get
     * the true byte count from the dispatcher ({@code VariableLengthDecoders.decode}'s
     * {@code requestBytes}); extension requests have no such parameter, so any extension list this
     * long is short by the same amount.
     */
    @Override
    public XRequest decodeRequest(int minorOpcode, com.github.moaxcp.x11.protocol.X11Input in)
            throws IOException {
        if ((minorOpcode & 0xFF) == RECTANGLES) {
            return readRectangles(in);
        }
        return null;
    }

    /**
     * {@code ShapeRectangles} as it arrives: the fixed part, then as many rectangles as the request's
     * own length leaves. Nothing inside the request says how many there are - the count comes from
     * the length, which is what the generated reader gets wrong.
     */
    private static Rectangles readRectangles(com.github.moaxcp.x11.protocol.X11Input in)
            throws IOException {
        in.readPad(1);                              // the minor opcode, already known
        int lengthUnits = Short.toUnsignedInt(in.readCard16());
        byte operation = in.readCard8();
        byte destinationKind = in.readCard8();
        byte ordering = in.readByte();
        in.readPad(1);
        int window = in.readCard32();
        short xOffset = in.readInt16();
        short yOffset = in.readInt16();
        // Sixteen bytes of header and fixed fields are behind us; eight bytes a rectangle.
        int count = Math.max(lengthUnits * 4 - 16, 0) / 8;
        List<com.github.moaxcp.x11.protocol.xproto.Rectangle> rectangles = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            rectangles.add(com.github.moaxcp.x11.protocol.xproto.Rectangle.readRectangle(in));
        }
        return Rectangles.builder()
                .operation(operation)
                .destinationKind(destinationKind)
                .ordering(ordering)
                .destinationWindow(window)
                .xOffset(xOffset)
                .yOffset(yOffset)
                .rectangles(Lists.immutable.withAll(rectangles))
                .build();
    }

    private void rectangles(XServer server, ClientConnection client, Rectangles request) {
        XWindow window = requireWindow(server, client, request.getDestinationWindow());
        int kind = unsigned(request.getDestinationKind());
        checkKind(kind);
        int operation = unsigned(request.getOperation());
        checkOperation(operation);
        List<Rectangle> rectangles = toAwt(request.getRectangles());
        log.debug("SHAPE rectangles on {} kind {} op {} count {}", window,
                ShapeState.kindName(kind), operation, rectangles.size());
        apply(server, window, kind, operation,
                ShapeState.Shape.ofRectangles(rectangles, request.getXOffset(), request.getYOffset()));
    }

    private void mask(XServer server, ClientConnection client, Mask request) {
        XWindow window = requireWindow(server, client, request.getDestinationWindow());
        int kind = unsigned(request.getDestinationKind());
        checkKind(kind);
        int operation = unsigned(request.getOperation());
        checkOperation(operation);
        if (request.getSourceBitmap() == 0) {
            // A None source is how a client removes a shape, so it is not a bad pixmap.
            apply(server, window, kind, operation, ShapeState.Shape.ofRectangles(List.of(), 0, 0));
            return;
        }
        XPixmap bitmap = (XPixmap) server.resources().require(client, request.getSourceBitmap(),
                XResource.CLASS_PIXMAP, false);
        // A shape mask is one bit deep; any other depth would silently mean something else.
        if (bitmap.depth() != 1) {
            throw ProtocolException.badMatch();
        }
        apply(server, window, kind, operation,
                ShapeState.Shape.ofMask(bitmap, request.getXOffset(), request.getYOffset()));
    }

    private void combine(XServer server, ClientConnection client, Combine request) {
        XWindow destination = requireWindow(server, client, request.getDestinationWindow());
        int destinationKind = unsigned(request.getDestinationKind());
        checkKind(destinationKind);
        int operation = unsigned(request.getOperation());
        checkOperation(operation);

        ShapeState.Shape source;
        if (request.getSourceWindow() == 0) {
            // A None source window contributes nothing, which turns Set into "remove the shape".
            source = new ShapeState.Shape();
        } else {
            int sourceKind = unsigned(request.getSourceKind());
            checkKind(sourceKind);
            XWindow sourceWindow = requireWindow(server, client, request.getSourceWindow());
            source = state(sourceWindow).shape(sourceKind).copy();
            if (!source.isSet()) {
                // An unshaped source window stands for its own rectangle, as the protocol says.
                source = ShapeState.Shape.ofRectangles(
                        List.of(new Rectangle(0, 0, sourceWindow.width(), sourceWindow.height())),
                        0, 0);
            }
            // The request offsets place the whole source shape, its own shape offset included.
            source.setOrigin(source.originX() + request.getXOffset(),
                    source.originY() + request.getYOffset());
        }
        apply(server, destination, destinationKind, operation, source);
    }

    private void offset(XServer server, ClientConnection client, Offset request) {
        XWindow window = requireWindow(server, client, request.getDestinationWindow());
        int kind = unsigned(request.getDestinationKind());
        checkKind(kind);
        ShapeState.Shape shape = state(window).shape(kind);
        if (!shape.isSet()) {
            // Offsetting a shape that was never set has nothing to move.
            return;
        }
        shape.setOrigin(shape.originX() + request.getXOffset(),
                shape.originY() + request.getYOffset());
        changed(server, window, kind);
    }

    private void queryExtents(XServer server, ClientConnection client, QueryExtents request) {
        XWindow window = requireWindow(server, client, request.getDestinationWindow());
        ShapeState state = state(window);
        ShapeState.Shape bounding = state.shape(ShapeState.BOUNDING);
        ShapeState.Shape clip = state.shape(ShapeState.CLIP);
        Rectangle boundingExtents = extents(bounding, window, true, false);
        Rectangle clipExtents = extents(clip, window, false, false);
        client.sendReply(QueryExtentsReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .boundingShaped(bounding.isSet())
                .clipShaped(clip.isSet())
                .boundingShapeExtentsX((short) boundingExtents.x)
                .boundingShapeExtentsY((short) boundingExtents.y)
                .boundingShapeExtentsWidth((short) boundingExtents.width)
                .boundingShapeExtentsHeight((short) boundingExtents.height)
                .clipShapeExtentsX((short) clipExtents.x)
                .clipShapeExtentsY((short) clipExtents.y)
                .clipShapeExtentsWidth((short) clipExtents.width)
                .clipShapeExtentsHeight((short) clipExtents.height)
                .build());
    }

    private void selectInput(XServer server, ClientConnection client, SelectInput request) {
        XWindow window = requireWindow(server, client, request.getDestinationWindow());
        synchronized (notifyClients) {
            Set<ClientConnection> clients = notifyClients.get(window);
            if (request.isEnable()) {
                if (clients == null) {
                    clients = Collections.newSetFromMap(new IdentityHashMap<ClientConnection, Boolean>());
                    notifyClients.put(window, clients);
                }
                clients.add(client);
            } else if (clients != null) {
                clients.remove(client);
                if (clients.isEmpty()) {
                    notifyClients.remove(window);
                }
            }
        }
    }

    private void inputSelected(XServer server, ClientConnection client, InputSelected request) {
        XWindow window = requireWindow(server, client, request.getDestinationWindow());
        boolean enabled;
        synchronized (notifyClients) {
            Set<ClientConnection> clients = notifyClients.get(window);
            enabled = clients != null && clients.contains(client);
        }
        client.sendReply(InputSelectedReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .enabled(enabled)
                .build());
    }

    /**
     * {@code ShapeGetRectangles}: the window's shape as rectangles, <b>banded</b>.
     *
     * <p>Measured against Xvfb 2026-10-04 (a window whose shape was set with one rectangle, three
     * times - {@code Unsorted}, {@code YSorted} and {@code YXBanded}): a reference answers
     * <b>{@code YXBanded} (3) every time</b> and the same rectangles, while this server answered
     * {@code Unsorted} (0) with a comment promising nothing. The X Test Suite reads that field as a
     * contract - "Expected order of 3, Received 0" in five of its eleven SHAPE cases - and a client
     * that merges the list does too. So the list is now put in that order rather than the answer
     * being dressed up: sorted by y then x, with rectangles of the same band merged when they touch.
     *
     * <p>A mask is decomposed into its own rectangles here (it used to be answered as one big
     * rectangle, "the closest honest answer"), which is both more of the truth and already banded by
     * construction.
     */
    private void getRectangles(XServer server, ClientConnection client, GetRectangles request) {
        XWindow window = requireWindow(server, client, request.getWindow());
        int kind = unsigned(request.getSourceKind());
        checkKind(kind);
        ShapeState.Shape shape = state(window).shape(kind);
        List<Rectangle> rectangles;
        if (!shape.isSet()) {
            // An unshaped window is its own rectangle - the *bounding* one including the border, and
            // one smaller in each dimension than QueryExtents reports (measured; see extents()).
            rectangles = List.of(extents(shape, window, kind == ShapeState.BOUNDING, true));
        } else {
            rectangles = band(shape.absoluteRectangles());
        }
        List<com.github.moaxcp.x11.protocol.xproto.Rectangle> reply =
                new ArrayList<>(rectangles.size());
        for (Rectangle rectangle : rectangles) {
            reply.add(toProtocol(rectangle));
        }
        client.sendReply(GetRectanglesReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .ordering((byte) ORDERING_YX_BANDED)
                .rectangles(Lists.immutable.ofAll(reply))
                .build());
    }

    /** The ordering this server answers with, which is what a reference answers (measured). */
    private static final int ORDERING_YX_BANDED = 3;

    /**
     * Sorts a rectangle list into the yx-banded form and merges what that form considers one
     * rectangle: same band (same y and height) and overlapping or touching x ranges.
     */
    private static List<Rectangle> band(List<Rectangle> rectangles) {
        List<Rectangle> banded = new ArrayList<>();
        for (Rectangle rectangle : rectangles) {
            if (rectangle.width <= 0 || rectangle.height <= 0) {
                continue;
            }
            Rectangle merged = rectangle;
            for (int i = 0; i < banded.size(); i++) {
                Rectangle other = banded.get(i);
                if (other.y == merged.y && other.height == merged.height
                        && merged.x <= other.x + other.width
                        && other.x <= merged.x + merged.width) {
                    int x1 = Math.min(other.x, merged.x);
                    int x2 = Math.max(other.x + other.width, merged.x + merged.width);
                    merged = new Rectangle(x1, other.y, x2 - x1, other.height);
                    banded.remove(i);
                    i = -1;                             // the merge can make it touch another
                }
            }
            banded.add(merged);
        }
        banded.sort(java.util.Comparator.comparingInt((Rectangle r) -> r.y)
                .thenComparingInt(r -> r.x));
        return banded;
    }

    // ------------------------------------------------------------------
    // Shape bookkeeping
    // ------------------------------------------------------------------

    /**
     * Combines {@code source} into the window's shape of {@code kind} and tells everyone who cares.
     * Both operands are materialized in window coordinates first, so a shape that used an offset or
     * a mask combines correctly with one that did not.
     */
    private void apply(XServer server, XWindow window, int kind, int operation,
                       ShapeState.Shape source) {
        ShapeState state = state(window);
        ShapeState.Shape destination = state.shape(kind);
        if (operation == ShapeState.OP_SET) {
            destination.copyFrom(source);
        } else {
            // Invert is the source minus the destination; every other operation reads left to right.
            List<Rectangle> left = operation == ShapeState.OP_INVERT
                    ? source.absoluteRectangles() : destination.absoluteRectangles();
            List<Rectangle> right = operation == ShapeState.OP_INVERT
                    ? destination.absoluteRectangles() : source.absoluteRectangles();
            List<Rectangle> combined = switch (operation) {
                case ShapeState.OP_UNION -> ShapeState.union(left, right);
                case ShapeState.OP_INTERSECT -> ShapeState.intersect(left, right);
                default -> ShapeState.subtract(left, right);
            };
            destination.copyFrom(ShapeState.Shape.ofRectangles(combined, 0, 0));
        }
        changed(server, window, kind);
    }

    /** A shape changed: repaint, and notify the clients that asked for shape events. */
    private void changed(XServer server, XWindow window, int kind) {
        if (kind == ShapeState.BOUNDING) {
            // The window carries its own bounding region, and the region code reads it per drawing
            // request: keep the two in step, and repaint what the change took away or added.
            List<Rectangle> before = window.coveredRegion();
            pushBoundingShape(window);
            List<Rectangle> after = window.coveredRegion();
            server.windows().shapeChanged(window, ShapeState.subtract(before, after),
                    ShapeState.subtract(after, before));
        } else {
            pushBoundingShape(window);
        }
        server.markScreenDirty(window);
        Set<ClientConnection> clients;
        synchronized (notifyClients) {
            Set<ClientConnection> registered = notifyClients.get(window);
            clients = registered == null ? Set.of() : Set.copyOf(registered);
        }
        if (clients.isEmpty()) {
            return;
        }
        ShapeState.Shape shape = state(window).shape(kind);
        // A ShapeNotify's extents are the rectangles' own convention (measured: one less than
        // QueryExtents for an unshaped window with a border).
        Rectangle extents = extents(shape, window, kind == ShapeState.BOUNDING, true);
        for (ClientConnection recipient : clients) {
            server.events().send(recipient, NotifyEvent.builder()
                    .firstEventOffset(firstEvent())
                    .sentEvent(false)
                    .shapeKind((byte) kind)
                    .sequenceNumber(recipient.sequenceNumber())
                    .affectedWindow(window.id())
                    .extentsX((short) extents.x)
                    .extentsY((short) extents.y)
                    .extentsWidth((short) extents.width)
                    .extentsHeight((short) extents.height)
                    .serverTime(server.events().time())
                    .shaped(shape.isSet())
                    .build());
        }
    }

    /** A shape's extents, falling back to the window's rectangle when the shape is unset. */
    /**
     * The extents of one shape, with an <b>unshaped window's own rectangle</b> standing in for a shape
     * that was never set - and it is not simply the window's size, which is what the SHAPE set of the
     * X Test Suite found (A50).
     *
     * <p>Measured against Xvfb, a 60x40 window with a one pixel border: an <em>unshaped</em> window
     * answers bounding extents **-1,-1 62x42** and clip extents **0,0 60x40**, while
     * {@code ShapeGetRectangles} for the same window answers **-1,-1 61x41** for the bounding shape -
     * one less in each dimension than {@code ShapeQueryExtents}, because a region's extents are
     * half-open. The bounding shape of a window therefore *includes its border* (the border lies
     * outside the window's own coordinate origin), and the clip shape, which no client may enlarge,
     * does not. With a border of nought - the case this server answered correctly by accident - the
     * two conventions agree, which is why nothing noticed.
     */
    private static Rectangle extents(ShapeState.Shape shape, XWindow window, boolean bounding,
                                     boolean rectangles) {
        if (shape.isSet()) {
            return shape.extents();
        }
        int border = bounding ? window.borderWidth() : 0;
        int shrink = bounding && rectangles ? 1 : 0;
        return new Rectangle(-border, -border,
                window.width() + 2 * border - shrink, window.height() + 2 * border - shrink);
    }

    private static XWindow requireWindow(XServer server, ClientConnection client, int id) {
        return (XWindow) server.resources().require(client, id, XResource.CLASS_WINDOW, false);
    }

    private static void checkKind(int kind) {
        if (kind < ShapeState.BOUNDING || kind > ShapeState.INPUT) {
            throw ProtocolException.badValue(kind);
        }
    }

    private static void checkOperation(int operation) {
        if (operation < ShapeState.OP_SET || operation > ShapeState.OP_INVERT) {
            throw ProtocolException.badValue(operation);
        }
    }

    private static int unsigned(byte value) {
        return Byte.toUnsignedInt(value);
    }

    private static List<Rectangle> toAwt(
            ImmutableList<com.github.moaxcp.x11.protocol.xproto.Rectangle> source) {
        List<Rectangle> rectangles = new ArrayList<>(source.size());
        for (com.github.moaxcp.x11.protocol.xproto.Rectangle rectangle : source) {
            rectangles.add(new Rectangle(rectangle.getX(), rectangle.getY(),
                    Short.toUnsignedInt(rectangle.getWidth()),
                    Short.toUnsignedInt(rectangle.getHeight())));
        }
        return rectangles;
    }

    private static com.github.moaxcp.x11.protocol.xproto.Rectangle toProtocol(Rectangle rectangle) {
        return com.github.moaxcp.x11.protocol.xproto.Rectangle.builder()
                .x((short) rectangle.x)
                .y((short) rectangle.y)
                .width((short) rectangle.width)
                .height((short) rectangle.height)
                .build();
    }
}
