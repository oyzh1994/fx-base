package cn.oyzh.fx.xserver.extension;

import com.github.moaxcp.x11.protocol.XProtocolPlugin;
import com.github.moaxcp.x11.protocol.XRequest;
import com.github.moaxcp.x11.protocol.damage.Add;
import com.github.moaxcp.x11.protocol.damage.Create;
import com.github.moaxcp.x11.protocol.damage.DamagePlugin;
import com.github.moaxcp.x11.protocol.damage.Destroy;
import com.github.moaxcp.x11.protocol.damage.NotifyEvent;
import com.github.moaxcp.x11.protocol.damage.QueryVersion;
import com.github.moaxcp.x11.protocol.damage.QueryVersionReply;
import com.github.moaxcp.x11.protocol.damage.Subtract;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.client.ClientConnection;
import cn.oyzh.fx.xserver.client.ProtocolException;
import cn.oyzh.fx.xserver.extension.Extension;
import cn.oyzh.fx.xserver.resource.RegionResource;
import cn.oyzh.fx.xserver.resource.ShapeState;
import cn.oyzh.fx.xserver.resource.XPixmap;
import cn.oyzh.fx.xserver.resource.XResource;
import cn.oyzh.fx.xserver.resource.XWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * DAMAGE: a client that wants to be <em>told</em> what changed, instead of being asked to draw again.
 *
 * <p>Every other way this server says anything about a window's pixels goes the wrong way round. An
 * {@code Expose} means "draw here again", which is the right answer when the server has lost the
 * pixels and no answer at all when the client is the one that wants to know - a screencast, a
 * compositor, a magnifier, anything that reads a window's pixels and needs to hear when they change.
 * Without this extension those clients poll: a screen recorder re-reads the whole window every frame
 * and copies pixels nobody looked at.
 *
 * <p>The model is X's own: a client creates a damage object on a drawable, chooses a <em>level</em>,
 * and the server sends it a {@code DamageNotify} with the area that changed and the drawable's
 * geometry. The levels are the part worth measuring rather than reading, and they were measured
 * against a reference server with {@code tools/DamageProbe}:
 *
 * <ul>
 *   <li>{@code RawRectangles} (0) reports every change as it happens, with that change's own area,
 *       and forgets it as it reports - so the next change reports again. This is the level a screen
 *       recorder uses.</li>
 *   <li>{@code BoundingBox} (2) reports nothing until a {@code Subtract} asks; the change that
 *       follows one is reported once, as the bounding box of everything that accumulated since. This
 *       server's output for this level is identical to a reference server's, line for line.</li>
 *   <li>{@code NonEmpty} (3) does the same thing with one measured difference, and it is recorded
 *       rather than copied because the mechanism was not found: on the reference the accumulated
 *       damage is **not** cleared by a subtract, so its next report carries everything since the
 *       object was created (the whole window in the probe's run), where this server reports what has
 *       changed since the subtract. Both report once per subtract; this server's area is the smaller
 *       one, and every pixel in it really did change.</li>
 * </ul>
 *
 * <p>What a notify carries is a bounding box on every path - the rectangles themselves live in the
 * damage region, which a {@code Subtract} can copy into a region of the client's own - and the area
 * is in the drawable's own coordinates while the geometry is where it sits on the screen.
 *
 * <p><b>The server's own painting counts as damage too.</b> A reference's accumulated damage covers
 * the whole window once it has been mapped, which is the background this server paints when a window
 * appears; a version of this that hooked only the drawing requests reported what the client drew and
 * nothing else, which is exactly the half a screencast does not need.</p>
 *
 * <p>{@code DeltaRectangles} (1) is answered like the two bounding levels, which is what a reference
 * server was measured to do ({@code DamageProbe 1}: nothing until a subtract, then one report per
 * change with its own area) - the specification describes it as reporting the difference between two
 * damage regions, and on this reference the difference and the box come to the same thing.
 */
public final class DamageExtension implements Extension {

    /** {@code ReportLevel}, X's own numbering - and not the order the names suggest. */
    private static final int RAW_RECTANGLES = 0;
    private static final int DELTA_RECTANGLES = 1;
    private static final int BOUNDING_BOX = 2;
    private static final int NON_EMPTY = 3;

    private static final int MAJOR_VERSION = 1;
    private static final int MINOR_VERSION = 1;

    /** Resource class of a damage object, shared by every one this extension creates. */
    private static final int WATCHER_CLASS = XResource.newExtensionClass();

    private static final Logger log = LoggerFactory.getLogger(DamageExtension.class);

    private final DamagePlugin plugin = new DamagePlugin();

    /**
     * The damage objects by the drawable they watch, which is how the drawing path finds them. Keyed
     * by identity: a destroyed window's id can be handed out again, and a new window is a different
     * drawable however it is numbered.
     */
    private final Map<XResource, List<Watcher>> byDrawable = new IdentityHashMap<>();

    /**
     * False until a client creates one, so the drawing path's call costs a field read when nobody is
     * watching - which is every drawing request of every ordinary client.
     */
    private volatile boolean watching;

    /** One damage object: what it watches, what it reports, and what has changed since it last did. */
    private static final class Watcher extends XResource {

        private final XResource drawable;
        private final int level;

        /** The damage since the last report, in the drawable's own coordinates. */
        private final List<Rectangle> damaged = new ArrayList<>();

        /**
         * Whether the next change is to be reported. {@code RawRectangles} reports every change, so it
         * starts true; the bounding levels report only once a {@code Subtract} has asked, which is
         * what a reference server was measured to do.
         */
        private boolean reportAfterSubtract;

        Watcher(int id, ClientConnection owner, XResource drawable, int level) {
            super(id, WATCHER_CLASS, owner);
            this.drawable = drawable;
            this.level = level;
            this.reportAfterSubtract = level == RAW_RECTANGLES;
        }

        @Override
        public String typeName() {
            return "damage";
        }
    }

    @Override
    public String name() {
        return "DAMAGE";
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
        } else if (request instanceof Create create) {
            create(server, client, create);
        } else if (request instanceof Destroy destroy) {
            Watcher watcher = requireWatcher(server, client, destroy.getDamage());
            forget(watcher.drawable, watcher);
            // Out of the resource table too, so the client can use the id again - which is what a
            // client that creates and destroys damage objects in a loop depends on.
            server.resources().free(watcher.id());
        } else if (request instanceof Subtract subtract) {
            subtract(server, client, subtract);
        } else if (request instanceof Add add) {
            add(server, client, add);
        } else {
            throw ProtocolException.unimplemented("DAMAGE " + request.getClass().getSimpleName());
        }
    }

    @Override
    public void clientDisconnected(XServer server, ClientConnection client) {
        // The resources themselves are freed with the connection; what has to go is the drawing
        // path's index into them, or a draw would walk a list of objects nobody owns.
        for (XResource drawable : List.copyOf(byDrawable.keySet())) {
            for (Watcher watcher : List.copyOf(byDrawable.getOrDefault(drawable, List.of()))) {
                if (watcher.owner() == client) {
                    forget(drawable, watcher);
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // Requests
    // ------------------------------------------------------------------

    private void queryVersion(ClientConnection client, QueryVersion request) {
        int major = (int) Math.min(MAJOR_VERSION,
                Integer.toUnsignedLong(request.getClientMajorVersion()));
        int minor = major < MAJOR_VERSION ? 0
                : (int) Math.min(MINOR_VERSION,
                        Integer.toUnsignedLong(request.getClientMinorVersion()));
        client.sendReply(QueryVersionReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .majorVersion(major)
                .minorVersion(minor)
                .build());
    }

    private void create(XServer server, ClientConnection client, Create request) {
        XResource drawable = server.resources().requireDrawable(client, request.getDrawable());
        int level = request.getLevel() & 0xFF;
        if (level != RAW_RECTANGLES && level != DELTA_RECTANGLES && level != BOUNDING_BOX
                && level != NON_EMPTY) {
            throw ProtocolException.badValue(level);
        }
        if (!client.ownsIdRange(request.getDamage()) || server.resources().contains(request.getDamage())) {
            throw ProtocolException.badIdChoice(request.getDamage());
        }
        Watcher watcher = new Watcher(request.getDamage(), client, drawable, level);
        server.resources().add(watcher);
        byDrawable.computeIfAbsent(drawable, key -> new ArrayList<>()).add(watcher);
        watching = true;
        if (log.isDebugEnabled()) {
            log.debug("damage 0x{} on drawable 0x{} at level {}",
                    Integer.toHexString(watcher.id()), Integer.toHexString(drawable.id()), level);
        }
    }

    /**
     * {@code DamageSubtract}: what the client has caught up with is handed to it and thrown away.
     *
     * <p>{@code repair}, when it names a region, is filled with the damage that has accumulated - the
     * rectangles themselves, which is the only way a client ever sees them, since a notify carries a
     * bounding box. {@code parts}, when it names one, keeps only the damage inside it. Either way the
     * damage is cleared and the next change is reported once, which is what a reference server does.
     */
    private void subtract(XServer server, ClientConnection client, Subtract request) {
        Watcher watcher = requireWatcher(server, client, request.getDamage());
        if (request.getParts() != 0) {
            RegionResource parts = region(server, client, request.getParts());
            List<Rectangle> shared = ShapeState.intersect(List.copyOf(watcher.damaged),
                    parts.rectangles());
            watcher.damaged.clear();
            watcher.damaged.addAll(shared);
        }
        if (request.getRepair() != 0) {
            region(server, client, request.getRepair()).setRectangles(List.copyOf(watcher.damaged));
        }
        watcher.damaged.clear();
        watcher.reportAfterSubtract = true;
    }

    /**
     * {@code DamageAdd}: a client that drew into a drawable where this server cannot see it - a
     * shared-memory buffer, an offscreen renderer, a video decoder - says what it changed.
     */
    private void add(XServer server, ClientConnection client, Add request) {
        XResource drawable = server.resources().requireDrawable(client, request.getDrawable());
        damageAdded(server, drawable, region(server, client, request.getRegion()).rectangles());
    }

    // ------------------------------------------------------------------
    // The drawing path
    // ------------------------------------------------------------------

    /**
     * Records that part of a drawable changed, and reports it where the level asks for it.
     *
     * <p>Called from the one place every drawing request comes through, with the bounding box of what
     * the request wrote in <em>screen</em> coordinates - the space the rasteriser records in, and the
     * space the screen's own damage is marked in. A damage object's region is kept in the drawable's
     * own coordinates, which is the space a client reads a notify's area in.
     */
    public static void drew(XServer server, XResource drawable, Rectangle bounds) {
        DamageExtension damage = of(server);
        if (damage == null || !damage.watching || bounds == null || bounds.isEmpty()) {
            return;
        }
        Rectangle own = bounds;
        if (drawable instanceof XWindow window) {
            own = new Rectangle(bounds.x - window.absoluteX(), bounds.y - window.absoluteY(),
                    bounds.width, bounds.height);
        }
        damage.damageAdded(server, drawable, List.of(own));
    }

    /** Called when a drawable goes away: a damage object does not outlive what it watched. */
    public static void drawableGone(XServer server, XResource drawable) {
        DamageExtension damage = of(server);
        if (damage != null && damage.watching) {
            for (Watcher watcher : List.copyOf(damage.byDrawable.getOrDefault(drawable, List.of()))) {
                damage.forget(drawable, watcher);
                // Out of the resource table as well, or the client's id stays taken for good and a
                // client that recreates an object on a fresh window is refused its own id.
                server.resources().free(watcher.id());
            }
        }
    }

    private static DamageExtension of(XServer server) {
        Extension extension = server.extensions().byName("DAMAGE");
        return extension instanceof DamageExtension damage ? damage : null;
    }

    private void damageAdded(XServer server, XResource drawable, List<Rectangle> rectangles) {
        List<Watcher> onIt = byDrawable.get(drawable);
        if (onIt == null || onIt.isEmpty()) {
            return;
        }
        for (Watcher watcher : List.copyOf(onIt)) {
            if (watcher.level == RAW_RECTANGLES) {
                // Each change reports itself, and is forgotten as it is reported.
                notifyDamage(server, watcher, boundsOf(rectangles));
                watcher.damaged.clear();
                continue;
            }
            watcher.damaged.addAll(rectangles);
            if (watcher.reportAfterSubtract) {
                Rectangle area = boundsOf(watcher.damaged);
                watcher.damaged.clear();
                watcher.reportAfterSubtract = false;
                notifyDamage(server, watcher, area);
            }
        }
    }

    private void notifyDamage(XServer server, Watcher watcher, Rectangle area) {
        XResource drawable = watcher.drawable;
        boolean isWindow = drawable instanceof XWindow;
        int x = isWindow ? ((XWindow) drawable).absoluteX() : 0;
        int y = isWindow ? ((XWindow) drawable).absoluteY() : 0;
        int width = isWindow ? ((XWindow) drawable).width()
                : drawable instanceof XPixmap pixmap ? pixmap.width() : 0;
        int height = isWindow ? ((XWindow) drawable).height()
                : drawable instanceof XPixmap pixmap ? pixmap.height() : 0;
        server.events().send(watcher.owner(), NotifyEvent.builder()
                .firstEventOffset(firstEvent())
                .sentEvent(false)
                .level((byte) watcher.level)
                .sequenceNumber(watcher.owner().sequenceNumber())
                .drawable(drawable.id())
                .damage(watcher.id())
                .timestamp(server.events().time())
                .area(rectangle(area))
                .geometry(rectangle(new Rectangle(x, y, width, height)))
                .build());
    }

    private static com.github.moaxcp.x11.protocol.xproto.Rectangle rectangle(Rectangle area) {
        return com.github.moaxcp.x11.protocol.xproto.Rectangle.builder()
                .x((short) area.x)
                .y((short) area.y)
                .width((short) area.width)
                .height((short) area.height)
                .build();
    }

    /** The bounding box of a list of rectangles, which is what a notify carries. */
    private static Rectangle boundsOf(List<Rectangle> rectangles) {
        Rectangle bounds = null;
        for (Rectangle rectangle : rectangles) {
            bounds = bounds == null ? rectangle : bounds.union(rectangle);
        }
        return bounds == null ? new Rectangle(0, 0, 0, 0) : bounds;
    }

    // ------------------------------------------------------------------
    // Bookkeeping
    // ------------------------------------------------------------------

    private void forget(XResource drawable, Watcher watcher) {
        List<Watcher> list = byDrawable.get(drawable);
        if (list != null) {
            list.remove(watcher);
            if (list.isEmpty()) {
                byDrawable.remove(drawable);
            }
        }
        watching = !byDrawable.isEmpty();
    }

    private Watcher requireWatcher(XServer server, ClientConnection client, int id) {
        XResource resource = server.resources().require(client, id, WATCHER_CLASS, false);
        return (Watcher) resource;
    }

    /** A region of the client's, which is the XFIXES extension's resource. */
    private static RegionResource region(XServer server, ClientConnection client,
                                                         int id) {
        XResource resource = server.resources().require(client, id, RegionResource.CLASS,
                false);
        return (RegionResource) resource;
    }
}
