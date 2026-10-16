package cn.oyzh.fx.xserver.extension;

import com.github.moaxcp.x11.protocol.XProtocolPlugin;
import com.github.moaxcp.x11.protocol.XRequest;
import com.github.moaxcp.x11.protocol.xfixes.ChangeCursor;
import com.github.moaxcp.x11.protocol.xfixes.ChangeCursorByName;
import com.github.moaxcp.x11.protocol.xfixes.ChangeSaveSet;
import com.github.moaxcp.x11.protocol.xfixes.CopyRegion;
import com.github.moaxcp.x11.protocol.xfixes.CreatePointerBarrier;
import com.github.moaxcp.x11.protocol.xfixes.CreateRegion;
import com.github.moaxcp.x11.protocol.xfixes.CreateRegionFromBitmap;
import com.github.moaxcp.x11.protocol.xfixes.CreateRegionFromGC;
import com.github.moaxcp.x11.protocol.xfixes.CreateRegionFromPicture;
import com.github.moaxcp.x11.protocol.xfixes.CreateRegionFromWindow;
import com.github.moaxcp.x11.protocol.xfixes.CursorNotifyEvent;
import com.github.moaxcp.x11.protocol.xfixes.DeletePointerBarrier;
import com.github.moaxcp.x11.protocol.xfixes.DestroyRegion;
import com.github.moaxcp.x11.protocol.xfixes.ExpandRegion;
import com.github.moaxcp.x11.protocol.xfixes.FetchRegion;
import com.github.moaxcp.x11.protocol.xfixes.FetchRegionReply;
import com.github.moaxcp.x11.protocol.xfixes.GetCursorImage;
import com.github.moaxcp.x11.protocol.xfixes.GetCursorImageAndName;
import com.github.moaxcp.x11.protocol.xfixes.GetCursorImageAndNameReply;
import com.github.moaxcp.x11.protocol.xfixes.GetCursorImageReply;
import com.github.moaxcp.x11.protocol.xfixes.GetCursorName;
import com.github.moaxcp.x11.protocol.xfixes.GetCursorNameReply;
import com.github.moaxcp.x11.protocol.xfixes.HideCursor;
import com.github.moaxcp.x11.protocol.xfixes.IntersectRegion;
import com.github.moaxcp.x11.protocol.xfixes.InvertRegion;
import com.github.moaxcp.x11.protocol.xfixes.QueryVersion;
import com.github.moaxcp.x11.protocol.xfixes.QueryVersionReply;
import com.github.moaxcp.x11.protocol.xfixes.RegionExtents;
import com.github.moaxcp.x11.protocol.xfixes.SelectCursorInput;
import com.github.moaxcp.x11.protocol.xfixes.SelectSelectionInput;
import com.github.moaxcp.x11.protocol.xfixes.SelectionNotifyEvent;
import com.github.moaxcp.x11.protocol.xfixes.SetCursorName;
import com.github.moaxcp.x11.protocol.xfixes.SetGCClipRegion;
import com.github.moaxcp.x11.protocol.xfixes.SetPictureClipRegion;
import com.github.moaxcp.x11.protocol.xfixes.SetRegion;
import com.github.moaxcp.x11.protocol.xfixes.SetWindowShapeRegion;
import com.github.moaxcp.x11.protocol.xfixes.ShowCursor;
import com.github.moaxcp.x11.protocol.xfixes.SubtractRegion;
import com.github.moaxcp.x11.protocol.xfixes.TranslateRegion;
import com.github.moaxcp.x11.protocol.xfixes.UnionRegion;
import com.github.moaxcp.x11.protocol.xfixes.XfixesPlugin;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.client.ClientConnection;
import cn.oyzh.fx.xserver.client.ProtocolException;
import cn.oyzh.fx.xserver.display.Screen;
import cn.oyzh.fx.xserver.extension.ShapeExtension;
import cn.oyzh.fx.xserver.resource.RegionResource;
import cn.oyzh.fx.xserver.resource.ShapeState;
import cn.oyzh.fx.xserver.resource.XCursor;
import cn.oyzh.fx.xserver.resource.XGC;
import cn.oyzh.fx.xserver.resource.XPixmap;
import cn.oyzh.fx.xserver.resource.XResource;
import cn.oyzh.fx.xserver.resource.XWindow;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.primitive.ByteList;
import org.eclipse.collections.api.list.primitive.IntList;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.primitive.ByteLists;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import cn.oyzh.common.log.JulLog;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * XFIXES.
 *
 * <p>XFIXES is the extension toolkits probe before they use anything else, because it is the gate
 * for RENDER and Composite. Two of its requests carry real weight here: cursor images, which
 * clients read back to draw eye candy and to compare cursors, and regions, which are the same
 * rectangles-and-algebra the SHAPE extension works with and which {@code SetWindowShapeRegion}
 * hands straight to it.
 *
 * <p>The version reported is 4.0, the highest release whose requests are all answered. Requests
 * that this server cannot honour are rejected with {@code BadImplementation} rather than silently
 * ignored, so a client is never told something happened when nothing did. That covers the picture
 * requests, which have no backing resources, and the pointer barriers.
 *
 * <p>Both kinds of notification are delivered. {@code SelectSelectionInput} subscriptions are
 * answered with the three {@code SelectionNotify} subtypes, raised by {@link #notifySelection} from
 * the code that changes a selection's owner; {@code SelectCursorInput} subscriptions are answered by
 * {@link #notifyCursor} from the display tick, which is where the server knows what the pointer is
 * over. Both were measured against a reference server first - the specification does not say, for
 * instance, that a selection losing its owner window reports the time the owner <em>took</em> it
 * rather than now, nor that a cursor's serial is the cursor's own rather than a change counter.
 *
 * <p>{@code HideCursor}/{@code ShowCursor} are honoured: each is a counted request on one window,
 * and {@code InputState} skips the sprite while the pointer is in that window or one of its
 * descendants ({@link XWindow#cursorHideCount()}). The cursor is hidden there rather than
 * everywhere, which is what the extension defines and what a client asking for it means.
 *
 * <p>Known gaps, all deliberate:
 * <ul>
 *   <li>{@code ChangeCursorByName}, {@code CreateRegionFromPicture} and {@code SetPictureClipRegion}
 *       are refused. The first because there is nothing to copy: a reference server does nothing at
 *       all for a name - not even for {@code left_ptr} - since the name is looked up in the cursor
 *       font and that font's glyphs carry no names (measured, and the refusal is the more honest of
 *       the two answers). The other two because a picture here has no client clip of its own: its
 *       clip is recomputed from the window's visible region on every request.
 * </ul>
 */
public final class XFixesExtension implements Extension {

    /** The two requests here whose decoders this class supplies: see {@link #decodeRequest}. */
    private static final int CREATE_REGION = 5;
    private static final int SET_REGION = 11;


    /** XFIXES 4.0: everything below is answered, and nothing above it is claimed. */
    private static final int MAJOR_VERSION = 4;
    private static final int MINOR_VERSION = 0;

    /**
     * The version this extension reports, for the one place that has to say why pointer barriers
     * cannot exist here: XI 2.3's barrier release is real, but the barriers are made through
     * XFIXES 5.0's {@code CreatePointerBarrier} (see {@code XInputExtension.releaseBarrier}).
     */
    static String reportedVersion() {
        return MAJOR_VERSION + "." + MINOR_VERSION;
    }

    /** Resource class of an XFIXES region, shared by every region this extension creates. */

    private final XfixesPlugin plugin = new XfixesPlugin();

    /** The SHAPE extension, resolved lazily because registration order is not ours to pick. */
    private ShapeExtension shape;

    /**
     * One {@code SelectSelectionInput}: which window the client wants the event reported against,
     * which selection it is watching, and the mask it asked for.
     *
     * <p>The selection is part of the subscription and not only of the request: a client that
     * watches CLIPBOARD must not hear about PRIMARY, and a map keyed by window alone cannot tell
     * the two apart. The window is what the event carries, because that is what the client named.
     */
    private record SelectionSubscription(int window, int selection, int mask) {
    }

    /** Cursor subscriptions, per client and per window: a cursor belongs to a screen, not to a thing. */
    private final Map<ClientConnection, Map<Integer, Integer>> cursorInput = new IdentityHashMap<>();

    /**
     * Selection subscriptions, per client. The subscription lives as long as the window it was made
     * on does - measured against a reference server, which stops notifying the moment that window is
     * destroyed, since the window is the subscriber's resource.
     */
    private final Map<ClientConnection, List<SelectionSubscription>> selectionInput =
            new IdentityHashMap<>();

    /** The three selection notification subtypes, for the callers outside this extension. */
    public static final byte SELECTION_SET_OWNER = 0;
    public static final byte SELECTION_WINDOW_DESTROY = 1;
    public static final byte SELECTION_CLIENT_CLOSE = 2;

    @Override
    public String name() {
        return "XFIXES";
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
            queryVersion(client, queryVersion);
        } else if (request instanceof GetCursorImage getCursorImage) {
            getCursorImage(server, client, getCursorImage);
        } else if (request instanceof GetCursorImageAndName getCursorImageAndName) {
            getCursorImageAndName(server, client, getCursorImageAndName);
        } else if (request instanceof GetCursorName getCursorName) {
            getCursorName(server, client, getCursorName);
        } else if (request instanceof SelectSelectionInput selectSelectionInput) {
            selectSelectionInput(server, client, selectSelectionInput);
        } else if (request instanceof SelectCursorInput selectCursorInput) {
            selectCursorInput(server, client, selectCursorInput);
        } else if (request instanceof HideCursor hideCursor) {
            hideCursor(server, client, hideCursor);
        } else if (request instanceof ShowCursor showCursor) {
            showCursor(server, client, showCursor);
        } else if (request instanceof CreateRegion createRegion) {
            createRegion(server, client, createRegion);
        } else if (request instanceof SetRegion setRegion) {
            setRegion(server, client, setRegion);
        } else if (request instanceof DestroyRegion destroyRegion) {
            destroyRegion(server, client, destroyRegion);
        } else if (request instanceof CopyRegion copyRegion) {
            copyRegion(server, client, copyRegion);
        } else if (request instanceof UnionRegion unionRegion) {
            unionRegion(server, client, unionRegion);
        } else if (request instanceof IntersectRegion intersectRegion) {
            intersectRegion(server, client, intersectRegion);
        } else if (request instanceof SubtractRegion subtractRegion) {
            subtractRegion(server, client, subtractRegion);
        } else if (request instanceof InvertRegion invertRegion) {
            invertRegion(server, client, invertRegion);
        } else if (request instanceof TranslateRegion translateRegion) {
            translateRegion(server, client, translateRegion);
        } else if (request instanceof RegionExtents regionExtents) {
            regionExtents(server, client, regionExtents);
        } else if (request instanceof ExpandRegion expandRegion) {
            expandRegion(server, client, expandRegion);
        } else if (request instanceof FetchRegion fetchRegion) {
            fetchRegion(server, client, fetchRegion);
        } else if (request instanceof CreateRegionFromBitmap createRegionFromBitmap) {
            createRegionFromBitmap(server, client, createRegionFromBitmap);
        } else if (request instanceof CreateRegionFromWindow createRegionFromWindow) {
            createRegionFromWindow(server, client, createRegionFromWindow);
        } else if (request instanceof CreateRegionFromGC createRegionFromGC) {
            createRegionFromGC(server, client, createRegionFromGC);
        } else if (request instanceof SetGCClipRegion setGCClipRegion) {
            setGCClipRegion(server, client, setGCClipRegion);
        } else if (request instanceof SetWindowShapeRegion setWindowShapeRegion) {
            setWindowShapeRegion(server, client, setWindowShapeRegion);
        } else if (request instanceof SetCursorName setCursorName) {
            setCursorName(server, client, setCursorName);
        } else if (request instanceof ChangeSaveSet changeSaveSet) {
            changeSaveSet(changeSaveSet);
        } else if (request instanceof DeletePointerBarrier deletePointerBarrier) {
            deletePointerBarrier(deletePointerBarrier);
        } else if (request instanceof CreateRegionFromPicture createRegionFromPicture) {
            throw ProtocolException.unimplemented("XFixesCreateRegionFromPicture");
        } else if (request instanceof SetPictureClipRegion setPictureClipRegion) {
            throw ProtocolException.unimplemented("XFixesSetPictureClipRegion");
        } else if (request instanceof ChangeCursor changeCursor) {
            changeCursor(server, client, changeCursor);
        } else if (request instanceof ChangeCursorByName changeCursorByName) {
            throw ProtocolException.unimplemented("XFixesChangeCursorByName");
        } else if (request instanceof CreatePointerBarrier createPointerBarrier) {
            throw ProtocolException.unimplemented("XFixesCreatePointerBarrier");
        } else {
            throw new IllegalArgumentException("unhandled XFIXES request " + request.getClass());
        }
    }

    @Override
    public void clientDisconnected(XServer server, ClientConnection client) {
        selectionInput.remove(client);
        cursorInput.remove(client);
    }

    /** This extension as registered on a server, or null when it is not there at all. */
    public static XFixesExtension of(XServer server) {
        Extension extension = server.extensions().byName("XFIXES");
        return extension instanceof XFixesExtension xfixes ? xfixes : null;
    }

    /**
     * Tells every client that subscribed to {@code selection} that its owner changed.
     *
     * <p>Called from the three places a selection can change hands - {@code SetSelectionOwner}, the
     * destruction of the owner window, and the owner's client going away - because none of them is
     * inside this extension. It is a no-op when XFIXES is not registered, which is how the callers
     * can use it unconditionally.
     *
     * @param owner the new owner, or 0 when the selection now belongs to nobody
     * @param timestamp when this change happened
     * @param selectionTimestamp when the selection was last <em>taken</em>, which is not the same
     *     thing: an owner window being destroyed reports the time its owner took the selection,
     *     because the selection has not changed hands - it has lost its holder
     * @param subtype one of {@link #SELECTION_SET_OWNER}, {@link #SELECTION_WINDOW_DESTROY},
     *     {@link #SELECTION_CLIENT_CLOSE}
     */
    public static void notifySelection(XServer server, int selection, int owner, int timestamp,
                                       int selectionTimestamp, byte subtype) {
        XFixesExtension xfixes = of(server);
        if (xfixes != null) {
            xfixes.selectionChanged(server, selection, owner, timestamp, selectionTimestamp, subtype);
        }
    }

    /**
     * The subscription a window carried goes with it.
     *
     * <p>Measured against a reference server: destroying the window a client subscribed on stops the
     * notifications, because the subscription is registered as a resource of that window. A client
     * that wants to go on hearing about the clipboard keeps its window alive.
     */
    public void windowDestroyed(XWindow window) {
        int id = window.id();
        for (List<SelectionSubscription> subscriptions : selectionInput.values()) {
            subscriptions.removeIf(subscription -> subscription.window() == id);
        }
        for (Map<Integer, Integer> masks : cursorInput.values()) {
            masks.remove(id);
        }
    }

    /**
     * Tells every client that subscribed to a screen's cursor that the displayed cursor changed.
     *
     * <p>Called from the display tick, which is where the server knows what the pointer is over. The
     * event names the window the subscription was made on - not the window the pointer is in - and
     * carries the cursor's own serial, so a client can tell a cursor it has seen before from a
     * different one.
     */
    public static void notifyCursor(XServer server, Screen screen, int cursorSerial,
                                    int timestamp) {
        XFixesExtension xfixes = of(server);
        if (xfixes != null) {
            xfixes.cursorChanged(server, screen, cursorSerial, timestamp);
        }
    }

    private void cursorChanged(XServer server, Screen screen, int cursorSerial, int timestamp) {
        List<Map.Entry<ClientConnection, Map<Integer, Integer>>> subscriptions =
                new ArrayList<>(cursorInput.entrySet());
        for (Map.Entry<ClientConnection, Map<Integer, Integer>> entry : subscriptions) {
            ClientConnection recipient = entry.getKey();
            for (Map.Entry<Integer, Integer> subscription : entry.getValue().entrySet()) {
                if ((subscription.getValue() & 1) == 0) {
                    continue;
                }
                // The window is what names the screen the client is watching: a cursor notification
                // is about a screen, and the window only says which one.
                XResource resource = server.resources().get(subscription.getKey());
                if (!(resource instanceof XWindow window) || window.screen() != screen) {
                    continue;
                }
                server.events().send(recipient, CursorNotifyEvent.builder()
                        .firstEventOffset(firstEvent())
                        .sentEvent(false)
                        .subtype((byte) 0)              // DisplayCursor
                        .sequenceNumber(recipient.sequenceNumber())
                        .window(subscription.getKey())
                        .cursorSerial(cursorSerial)
                        .timestamp(timestamp)
                        .name(0)                        // cursors here carry no name
                        .build());
            }
        }
    }

    private void selectionChanged(XServer server, int selection, int owner, int timestamp,
                                  int selectionTimestamp, byte subtype) {
        int wantBit = 1 << subtype;
        // Snapshot first: sending can close a connection, and a closed connection is dropped from
        // these tables by clientDisconnected while this loop is walking them.
        List<Map.Entry<ClientConnection, List<SelectionSubscription>>> subscriptions =
                new ArrayList<>(selectionInput.entrySet());
        for (Map.Entry<ClientConnection, List<SelectionSubscription>> entry : subscriptions) {
            ClientConnection recipient = entry.getKey();
            for (SelectionSubscription subscription : new ArrayList<>(entry.getValue())) {
                // Two ways not to be interested: the mask does not cover this subtype, or this is
                // not the selection the client asked about.
                if (subscription.selection() != selection
                        || (subscription.mask() & wantBit) == 0) {
                    continue;
                }
                server.events().send(recipient, SelectionNotifyEvent.builder()
                        .firstEventOffset(firstEvent())
                        .sentEvent(false)
                        .subtype(subtype)
                        .sequenceNumber(recipient.sequenceNumber())
                        .window(subscription.window())
                        .owner(owner)
                        .selection(selection)
                        .timestamp(timestamp)
                        .selectionTimestamp(selectionTimestamp)
                        .build());
            }
        }
    }

    /**
     * {@code XFixesCreateRegion} and {@code XFixesSetRegion} are decoded here for the reason
     * {@link cn.oyzh.fx.xserver.protocol.VariableLengthDecoders} gives for the sixteen core
     * requests it covers, and {@code ShapeExtension} gives for {@code ShapeRectangles}: the
     * generated reader sizes its rectangle list with {@code withInitialCapacity(length - javaStart)},
     * where {@code length} is the request length in <em>four byte units</em> and {@code javaStart} a
     * byte offset. This request's fixed part is eight bytes, so **zero, one or two rectangles** -
     * a request of eight, twelve or sixteen bytes - come out negative and are refused with
     * {@code BadRequest} before the handler sees them. Three rectangles (six units against a fixed
     * part of eight) are accepted, which is what makes this look like a rule about region sizes
     * rather than an arithmetic mistake.
     *
     * <p>A client reaches this the moment it builds a region for a shape, a clip or an input region
     * of its own, so the refusal is the ordinary small case: {@code XFixesCreateRegion} with one
     * rectangle. Nothing else here needs hand decoding - every other XFIXES request is a fixed size
     * or carries no list ({@code SetCursorName} carries a string, which the generated reader sizes
     * from its own length field and gets right).
     *
     * <p>As in {@code ShapeExtension}, the count can only be as good as the sixteen bit length field:
     * an extension request over 64K cannot describe itself in it, and the extension path is not
     * given the true byte count the way the core decoders are. A region of more than eight thousand
     * rectangles would be read short.
     */
    @Override
    public XRequest decodeRequest(int minorOpcode, com.github.moaxcp.x11.protocol.X11Input in)
            throws java.io.IOException {
        if ((minorOpcode & 0xFF) == CREATE_REGION) {
            return readCreateRegion(in);
        }
        if ((minorOpcode & 0xFF) == SET_REGION) {
            return readSetRegion(in);
        }
        return null;
    }

    /** {@code XFixesCreateRegion} as it arrives: the region, then as many rectangles as were sent. */
    private static CreateRegion readCreateRegion(com.github.moaxcp.x11.protocol.X11Input in)
            throws java.io.IOException {
        Region region = readRegion(in);
        return CreateRegion.builder()
                .region(region.id())
                .rectangles(Lists.immutable.withAll(region.rectangles()))
                .build();
    }

    /**
     * {@code XFixesSetRegion}, the other half of the same pair: it replaces a region's rectangles,
     * so a client that keeps a region and changes it - which is what a toolkit does with a shape it
     * recalculates - reaches it. Same arithmetic, same fixed part of eight bytes, so **one or two
     * rectangles** are refused; measured against a live server 2026-09-26 (one and two refused with
     * {@code initial capacity cannot be less than 0}, three accepted and then refused by the handler
     * for the region id the probe invented, which is how the threshold shows).
     */
    private static SetRegion readSetRegion(com.github.moaxcp.x11.protocol.X11Input in)
            throws java.io.IOException {
        Region region = readRegion(in);
        return SetRegion.builder()
                .region(region.id())
                .rectangles(Lists.immutable.withAll(region.rectangles()))
                .build();
    }

    /** The region id and rectangles these two requests both carry, in the order they arrive. */
    private record Region(int id, List<com.github.moaxcp.x11.protocol.xproto.Rectangle> rectangles) {
    }

    /**
     * The body both requests have: eight bytes of header and region behind the reader, then eight
     * bytes a rectangle. The count comes from the length that arrived - nothing inside the request
     * says how many rectangles there are, which is what the generated reader gets wrong.
     */
    private static Region readRegion(com.github.moaxcp.x11.protocol.X11Input in)
            throws java.io.IOException {
        in.readPad(1);                              // the minor opcode, already known
        int lengthUnits = Short.toUnsignedInt(in.readCard16());
        int region = in.readCard32();
        int count = Math.max(lengthUnits * 4 - 8, 0) / 8;
        List<com.github.moaxcp.x11.protocol.xproto.Rectangle> rectangles = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            rectangles.add(com.github.moaxcp.x11.protocol.xproto.Rectangle.readRectangle(in));
        }
        return new Region(region, rectangles);
    }

    // ------------------------------------------------------------------
    // Version and cursors
    // ------------------------------------------------------------------

    /**
     * The reply is capped at the major version the client asked for, as X defines: a client that
     * only knows 2.0 would otherwise be told about a version it cannot speak. A client that asks
     * for more than we have is told what we have, which is the point of the request.
     */
    private void queryVersion(ClientConnection client, QueryVersion request) {
        int major = MAJOR_VERSION;
        int minor = MINOR_VERSION;
        if (request.getClientMajorVersion() < MAJOR_VERSION) {
            major = request.getClientMajorVersion();
            minor = 0;
        }
        client.sendReply(QueryVersionReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .majorVersion(major)
                .minorVersion(minor)
                .build());
    }

    private void getCursorImage(XServer server, ClientConnection client, GetCursorImage request) {
        XCursor cursor = displayedCursor(server, client);
        client.sendReply(GetCursorImageReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .x((short) (server.inputFor(client).pointerX() - cursor.hotspotX()))
                .y((short) (server.inputFor(client).pointerY() - cursor.hotspotY()))
                .width((short) cursor.width())
                .height((short) cursor.height())
                .xhot((short) cursor.hotspotX())
                .yhot((short) cursor.hotspotY())
                .cursorSerial(cursor.serial())
                .cursorImage(cursorPixels(cursor))
                .build());
    }

    private void getCursorImageAndName(XServer server, ClientConnection client,
                                       GetCursorImageAndName request) {
        XCursor cursor = displayedCursor(server, client);
        client.sendReply(GetCursorImageAndNameReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .x((short) (server.inputFor(client).pointerX() - cursor.hotspotX()))
                .y((short) (server.inputFor(client).pointerY() - cursor.hotspotY()))
                .width((short) cursor.width())
                .height((short) cursor.height())
                .xhot((short) cursor.hotspotX())
                .yhot((short) cursor.hotspotY())
                .cursorSerial(cursor.serial())
                .cursorAtom(cursor.name())
                .cursorImage(cursorPixels(cursor))
                .name(name(cursor, server))
                .build());
    }

    /**
     * The cursor the pointer is showing - the window's own, or the nearest one it inherits - which
     * is what the two "get the cursor image" requests are defined to answer with.
     *
     * <p>Both of them used to answer {@link XServer#rootCursor()} with a comment saying the root
     * cursor never changes, which is true and beside the point: measured against a reference server
     * on 2026-09-26, Xvfb answers with the cursor the window was given (a glyph cursor defined on
     * the window came back as that glyph) while this server answered the arrow however the pointer
     * was sitting. The cursor *notifications* had it right all along - the tick reads
     * {@code displayedCursor()} and reports its serial - so this is the same reading, and the serial
     * travels with it: a cursor that has been shown before answers with the serial it had then, which
     * is the rule the notifications were measured to follow.
     */
    private static XCursor displayedCursor(XServer server, ClientConnection client) {
        XCursor displayed = server.inputFor(client).displayedCursor();
        return displayed != null ? displayed : server.rootCursor();
    }

    /**
     * Cursors here are built from bitmaps or glyphs and are never named, so the honest answer is
     * {@code None} with an empty name rather than an invented atom.
     */
    /**
     * The name a client gave this cursor, which is how one client tells another's cursors apart.
     * Measured against a reference: a name given is a name read back, and a cursor nobody named
     * answers {@code None} with nothing.
     */
    private void getCursorName(XServer server, ClientConnection client, GetCursorName request) {
        XCursor cursor = requireCursor(server, client, request.getCursor());
        client.sendReply(GetCursorNameReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .atom(cursor.name())
                .name(name(cursor, server))
                .build());
    }

    /** A cursor's name as the bytes of a reply, or nothing when it has none. */
    private ByteList name(XCursor cursor, XServer server) {
        if (cursor.name() == 0) {
            return ByteLists.immutable.empty();
        }
        String text = server.atoms().name(cursor.name());
        return text == null ? ByteLists.immutable.empty()
                : ByteLists.immutable.of(text.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));
    }

    private void setCursorName(XServer server, ClientConnection client, SetCursorName request) {
        XCursor cursor = requireCursor(server, client, request.getCursor());
        // The name is an atom, so the client can look it up, and an empty name takes one back off -
        // this server answers None for that, where a reference interns the empty string as an atom
        // and reports it (measured: 87 with no bytes, against 0 here). A client that tests for None
        // gets the sane answer; one that compares atoms it interned itself sees the difference.
        String name = new String(request.getName().toArray(),
                java.nio.charset.StandardCharsets.ISO_8859_1);
        cursor.setName(name.isEmpty() ? 0 : server.atoms().intern(name));
        server.markScreenDirty();
    }

    private static IntList cursorPixels(XCursor cursor) {
        int width = cursor.width();
        int height = cursor.height();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixels[y * width + x] = cursor.pixel(x, y);
            }
        }
        return IntLists.immutable.of(pixels);
    }

    // ------------------------------------------------------------------
    // Notifications, hiding
    // ------------------------------------------------------------------

    /**
     * Records a selection subscription, replacing whatever this client had for the same window and
     * selection. A mask of zero is how a client unsubscribes, and it cancels rather than adds.
     */
    private void selectSelectionInput(XServer server, ClientConnection client,
                                      SelectSelectionInput request) {
        XWindow window = requireWindow(server, client, request.getWindow());
        int mask = request.getEventMask();
        if (JulLog.isDebugEnabled()) {
            // Which selection a client asked to hear about, on which window, and with which bits:
            // the first thing to check when a notification never arrives, and the reason a
            // subscription is not simply "this client wants selection events".
            JulLog.debug("XFIXES 0x{} watches {} mask 0x{} on 0x{}", client.index(),
                    server.atoms().name(request.getSelection()), Integer.toHexString(mask),
                    Integer.toHexString(window.id()));
        }
        List<SelectionSubscription> subscriptions =
                selectionInput.computeIfAbsent(client, key -> new ArrayList<>());
        subscriptions.removeIf(subscription -> subscription.window() == window.id()
                && subscription.selection() == request.getSelection());
        if (mask != 0) {
            subscriptions.add(new SelectionSubscription(window.id(), request.getSelection(), mask));
        }
    }

    private void selectCursorInput(XServer server, ClientConnection client,
                                   SelectCursorInput request) {
        XWindow window = requireWindow(server, client, request.getWindow());
        recordMask(cursorInput, client, window.id(), request.getEventMask());
    }

    private void hideCursor(XServer server, ClientConnection client, HideCursor request) {
        XWindow window = requireWindow(server, client, request.getWindow());
        window.hideCursor();
        // The sprite is not part of the framebuffer, so hiding it is not a change to what a client
        // drew - but the frame has to go out for the sprite to disappear from it: a client that
        // hides the cursor and then draws nothing would otherwise keep the last frame, pointer and
        // all, on the screen until something else moved.
        server.markScreenDirty(window);
    }

    private void showCursor(XServer server, ClientConnection client, ShowCursor request) {
        XWindow window = requireWindow(server, client, request.getWindow());
        window.showCursor();
        server.markScreenDirty(window);
    }

    /**
     * Records a cursor subscription, dropping it when the mask is zero, which is how a client
     * unsubscribes.
     */
    private void recordMask(Map<ClientConnection, Map<Integer, Integer>> table,
                            ClientConnection client, int window, int mask) {
        Map<Integer, Integer> masks = table.computeIfAbsent(client, key -> new HashMap<>());
        if (mask == 0) {
            masks.remove(window);
        } else {
            masks.put(window, mask);
        }
    }

    private void changeSaveSet(ChangeSaveSet request) {
        // XFIXES extends the save set to windows owned by other clients. The core save set lives in
        // the window handlers and has no hook for that, so the request is accepted and ignored.
        JulLog.debug("XFIXES ChangeSaveSet ignored: only the core save set is tracked");
    }

    private void deletePointerBarrier(DeletePointerBarrier request) {
        // No barrier can have been created, so there is nothing to delete.
        JulLog.debug("XFIXES DeletePointerBarrier ignored: barriers are not implemented");
    }

    // ------------------------------------------------------------------
    // Regions
    // ------------------------------------------------------------------

    /**
     * {@code XFixesChangeCursor}: the destination cursor becomes the source - its image, its hotspot
     * and its serial - and stays the same *resource*, so a window already showing it shows the new
     * shape.
     *
     * <p>Measured against a reference server with {@code tools/ChangeCursorProbe}: after
     * {@code ChangeCursor(source, destination)} the window showing the destination reports the
     * source's shape and the source's serial, and **no cursor notification is raised** - a rewrite is
     * not a cursor change, which is why nothing here announces one.
     */
    private void changeCursor(XServer server, ClientConnection client, ChangeCursor request) {
        XCursor source = requireCursor(server, client, request.getSource());
        XCursor destination = requireCursor(server, client, request.getDestination());
        destination.adopt(source);
        // The sprite is not part of the framebuffer, so a frame has to go out for the new shape to
        // reach the screen: a client that rewrites a cursor and then draws nothing would otherwise
        // keep showing the old one.
        server.markScreenDirty();
    }

    private void createRegion(XServer server, ClientConnection client, CreateRegion request) {
        create(server, client, request.getRegion(), toAwt(request.getRectangles()));
    }

    private void setRegion(XServer server, ClientConnection client, SetRegion request) {
        requireRegion(server, client, request.getRegion())
                .setRectangles(toAwt(request.getRectangles()));
    }

    private void destroyRegion(XServer server, ClientConnection client, DestroyRegion request) {
        if (request.getRegion() == 0) {
            return;
        }
        requireRegion(server, client, request.getRegion());
        server.resources().free(request.getRegion());
    }

    private void copyRegion(XServer server, ClientConnection client, CopyRegion request) {
        RegionResource destination = requireRegion(server, client, request.getDestination());
        destination.setRectangles(requireRegion(server, client, request.getSource()).rectangles());
    }

    private void unionRegion(XServer server, ClientConnection client, UnionRegion request) {
        RegionResource destination = requireRegion(server, client, request.getDestination());
        destination.setRectangles(ShapeState.union(
                requireRegion(server, client, request.getSource1()).rectangles(),
                requireRegion(server, client, request.getSource2()).rectangles()));
    }

    private void intersectRegion(XServer server, ClientConnection client, IntersectRegion request) {
        RegionResource destination = requireRegion(server, client, request.getDestination());
        destination.setRectangles(ShapeState.intersect(
                requireRegion(server, client, request.getSource1()).rectangles(),
                requireRegion(server, client, request.getSource2()).rectangles()));
    }

    private void subtractRegion(XServer server, ClientConnection client, SubtractRegion request) {
        RegionResource destination = requireRegion(server, client, request.getDestination());
        destination.setRectangles(ShapeState.subtract(
                requireRegion(server, client, request.getSource1()).rectangles(),
                requireRegion(server, client, request.getSource2()).rectangles()));
    }

    private void invertRegion(XServer server, ClientConnection client, InvertRegion request) {
        RegionResource destination = requireRegion(server, client, request.getDestination());
        Rectangle bounds = toAwt(request.getBounds());
        destination.setRectangles(ShapeState.invert(
                requireRegion(server, client, request.getSource()).rectangles(), bounds));
    }

    private void translateRegion(XServer server, ClientConnection client, TranslateRegion request) {
        RegionResource region = requireRegion(server, client, request.getRegion());
        region.setRectangles(ShapeState.translate(region.rectangles(), request.getDx(),
                request.getDy()));
    }

    private void regionExtents(XServer server, ClientConnection client, RegionExtents request) {
        RegionResource destination = requireRegion(server, client, request.getDestination());
        Rectangle extents = ShapeState.extents(
                requireRegion(server, client, request.getSource()).rectangles());
        destination.setRectangles(extents == null ? List.of() : List.of(extents));
    }

    private void expandRegion(XServer server, ClientConnection client, ExpandRegion request) {
        RegionResource destination = requireRegion(server, client, request.getDestination());
        destination.setRectangles(ShapeState.expand(
                requireRegion(server, client, request.getSource()).rectangles(),
                Short.toUnsignedInt(request.getLeft()), Short.toUnsignedInt(request.getRight()),
                Short.toUnsignedInt(request.getTop()), Short.toUnsignedInt(request.getBottom())));
    }

    private void fetchRegion(XServer server, ClientConnection client, FetchRegion request) {
        List<Rectangle> rectangles = requireRegion(server, client, request.getRegion()).rectangles();
        Rectangle extents = ShapeState.extents(rectangles);
        List<com.github.moaxcp.x11.protocol.xproto.Rectangle> reply =
                new ArrayList<>(rectangles.size());
        for (Rectangle rectangle : rectangles) {
            reply.add(toProtocol(rectangle));
        }
        client.sendReply(FetchRegionReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .extents(extents == null ? emptyProtocolRectangle() : toProtocol(extents))
                .rectangles(Lists.immutable.ofAll(reply))
                .build());
    }

    private void createRegionFromBitmap(XServer server, ClientConnection client,
                                        CreateRegionFromBitmap request) {
        XPixmap bitmap = (XPixmap) server.resources().require(client, request.getBitmap(),
                XResource.CLASS_PIXMAP, false);
        if (bitmap.depth() != 1) {
            throw ProtocolException.badMatch();
        }
        create(server, client, request.getRegion(), ShapeState.maskRectangles(bitmap));
    }

    private void createRegionFromWindow(XServer server, ClientConnection client,
                                        CreateRegionFromWindow request) {
        XWindow window = requireWindow(server, client, request.getWindow());
        int kind = Byte.toUnsignedInt(request.getKind());
        checkShapeKind(kind);
        ShapeExtension shape = shape(server);
        if (shape == null) {
            throw ProtocolException.unimplemented("SHAPE is not registered");
        }
        create(server, client, request.getRegion(), shape.shapeRectangles(window, kind));
    }

    private void createRegionFromGC(XServer server, ClientConnection client,
                                    CreateRegionFromGC request) {
        XGC gc = (XGC) server.resources().require(client, request.getGc(), XResource.CLASS_GC, false);
        List<Rectangle> rectangles;
        if (gc.isClipByRectangles()) {
            rectangles = new ArrayList<>(gc.clipRectangles().size());
            for (XGC.Rectangle2 rectangle : gc.clipRectangles()) {
                rectangles.add(new Rectangle(rectangle.x() + gc.clipOriginX(),
                        rectangle.y() + gc.clipOriginY(), rectangle.width(), rectangle.height()));
            }
        } else if (gc.clipMask() instanceof XPixmap mask) {
            rectangles = ShapeState.translate(ShapeState.maskRectangles(mask), gc.clipOriginX(),
                    gc.clipOriginY());
        } else {
            // No clip at all means the region is the whole screen.
            rectangles = List.of(new Rectangle(0, 0, server.screen().width(),
                    server.screen().height()));
        }
        create(server, client, request.getRegion(), rectangles);
    }

    private void setGCClipRegion(XServer server, ClientConnection client, SetGCClipRegion request) {
        XGC gc = (XGC) server.resources().require(client, request.getGc(), XResource.CLASS_GC, false);
        if (request.getRegion() == 0) {
            // None resets the clip, which is a null mask rather than an empty rectangle list: an
            // empty list would clip everything away.
            gc.setClipMask(null);
            return;
        }
        // The rectangles are relative to the clip origin, which is where the region sits.
        List<XGC.Rectangle2> rectangles = new ArrayList<>();
        for (Rectangle rectangle : requireRegion(server, client, request.getRegion()).rectangles()) {
            rectangles.add(new XGC.Rectangle2(rectangle.x - request.getXOrigin(),
                    rectangle.y - request.getYOrigin(), rectangle.width, rectangle.height));
        }
        gc.setClipRectangles(rectangles, 0);
        gc.setClipOriginX(request.getXOrigin());
        gc.setClipOriginY(request.getYOrigin());
    }

    private void setWindowShapeRegion(XServer server, ClientConnection client,
                                      SetWindowShapeRegion request) {
        XWindow window = requireWindow(server, client, request.getDest());
        int kind = Byte.toUnsignedInt(request.getDestKind());
        checkShapeKind(kind);
        ShapeExtension shape = shape(server);
        if (shape == null) {
            throw ProtocolException.unimplemented("SHAPE is not registered");
        }
        // A None region removes the shape, which SHAPE spells as an empty rectangle list.
        List<Rectangle> rectangles = request.getRegion() == 0 ? List.of()
                : requireRegion(server, client, request.getRegion()).rectangles();
        shape.setShape(server, window, kind, rectangles, request.getXOffset(), request.getYOffset());
    }

    private void create(XServer server, ClientConnection client, int id, List<Rectangle> rectangles) {
        if (!client.ownsIdRange(id) || server.resources().contains(id)) {
            throw ProtocolException.badIdChoice(id);
        }
        server.resources().add(new RegionResource(id, client, rectangles));
    }

    private RegionResource requireRegion(XServer server, ClientConnection client, int id) {
        try {
            return (RegionResource) server.resources().require(client, id, RegionResource.CLASS, false);
        } catch (ProtocolException e) {
            // A region id belonging to another client stays an id choice error; anything else
            // about a region is the extension's own BadRegion.
            if (e.errorCode() == ProtocolException.ID_CHOICE) {
                throw e;
            }
            throw badRegion(id);
        }
    }

    private ProtocolException badRegion(int id) {
        return new ProtocolException((byte) (plugin.getFirstError() & 0xFF), id,
                "bad region 0x" + Integer.toHexString(id));
    }

    private static XWindow requireWindow(XServer server, ClientConnection client, int id) {
        return (XWindow) server.resources().require(client, id, XResource.CLASS_WINDOW, false);
    }

    /**
     * The resource class of an XFIXES region, for the DAMAGE extension: a {@code DamageSubtract}'s
     * repair region is one of these, and neither extension can name it without this.
     */
    static int regionClass() {
        return RegionResource.CLASS;
    }

    /** A cursor resource, which is a core resource rather than an extension one. */
    private static XCursor requireCursor(XServer server, ClientConnection client, int id) {
        return (XCursor) server.resources().require(client, id, XResource.CLASS_CURSOR, false);
    }

    private static void checkShapeKind(int kind) {
        if (kind < ShapeState.BOUNDING || kind > ShapeState.INPUT) {
            throw ProtocolException.badValue(kind);
        }
    }

    /** Resolves the SHAPE extension, which is where regions used as window shapes end up. */
    private ShapeExtension shape(XServer server) {
        if (shape == null) {
            shape = ShapeExtension.of(server);
        }
        return shape;
    }

    // ------------------------------------------------------------------
    // Conversions
    // ------------------------------------------------------------------

    private static List<Rectangle> toAwt(
            ImmutableList<com.github.moaxcp.x11.protocol.xproto.Rectangle> source) {
        List<Rectangle> rectangles = new ArrayList<>(source.size());
        for (com.github.moaxcp.x11.protocol.xproto.Rectangle rectangle : source) {
            rectangles.add(toAwt(rectangle));
        }
        return rectangles;
    }

    private static Rectangle toAwt(com.github.moaxcp.x11.protocol.xproto.Rectangle rectangle) {
        return new Rectangle(rectangle.getX(), rectangle.getY(),
                Short.toUnsignedInt(rectangle.getWidth()),
                Short.toUnsignedInt(rectangle.getHeight()));
    }

    private static com.github.moaxcp.x11.protocol.xproto.Rectangle toProtocol(Rectangle rectangle) {
        return com.github.moaxcp.x11.protocol.xproto.Rectangle.builder()
                .x((short) rectangle.x)
                .y((short) rectangle.y)
                .width((short) rectangle.width)
                .height((short) rectangle.height)
                .build();
    }

    private static com.github.moaxcp.x11.protocol.xproto.Rectangle emptyProtocolRectangle() {
        return com.github.moaxcp.x11.protocol.xproto.Rectangle.builder().build();
    }
}
