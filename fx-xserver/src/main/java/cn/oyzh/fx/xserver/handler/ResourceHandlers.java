package cn.oyzh.fx.xserver.handler;

import com.github.moaxcp.x11.protocol.xproto.AllocColor;
import com.github.moaxcp.x11.protocol.xproto.AllocColorCells;
import com.github.moaxcp.x11.protocol.xproto.AllocColorCellsReply;
import com.github.moaxcp.x11.protocol.xproto.AllocColorPlanes;
import com.github.moaxcp.x11.protocol.xproto.AllocColorPlanesReply;
import com.github.moaxcp.x11.protocol.xproto.AllocColorReply;
import com.github.moaxcp.x11.protocol.xproto.AllocNamedColor;
import com.github.moaxcp.x11.protocol.xproto.AllocNamedColorReply;
import com.github.moaxcp.x11.protocol.xproto.ChangeGC;
import com.github.moaxcp.x11.protocol.xproto.Char2b;
import com.github.moaxcp.x11.protocol.xproto.Charinfo;
import com.github.moaxcp.x11.protocol.xproto.CloseFont;
import com.github.moaxcp.x11.protocol.xproto.ColorFlag;
import com.github.moaxcp.x11.protocol.xproto.Coloritem;
import com.github.moaxcp.x11.protocol.xproto.CopyColormapAndFree;
import com.github.moaxcp.x11.protocol.xproto.CopyGC;
import com.github.moaxcp.x11.protocol.xproto.CreateColormap;
import com.github.moaxcp.x11.protocol.xproto.CreateCursor;
import com.github.moaxcp.x11.protocol.xproto.CreateGC;
import com.github.moaxcp.x11.protocol.xproto.CreateGlyphCursor;
import com.github.moaxcp.x11.protocol.xproto.CreatePixmap;
import com.github.moaxcp.x11.protocol.xproto.Fontprop;
import com.github.moaxcp.x11.protocol.xproto.FreeColormap;
import com.github.moaxcp.x11.protocol.xproto.FreeColors;
import com.github.moaxcp.x11.protocol.xproto.FreeCursor;
import com.github.moaxcp.x11.protocol.xproto.FreeGC;
import com.github.moaxcp.x11.protocol.xproto.FreePixmap;
import com.github.moaxcp.x11.protocol.xproto.Gc;
import com.github.moaxcp.x11.protocol.xproto.GetFontPath;
import com.github.moaxcp.x11.protocol.xproto.GetFontPathReply;
import com.github.moaxcp.x11.protocol.xproto.InstallColormap;
import com.github.moaxcp.x11.protocol.xproto.ListFonts;
import com.github.moaxcp.x11.protocol.xproto.ListFontsReply;
import com.github.moaxcp.x11.protocol.xproto.ListFontsWithInfo;
import com.github.moaxcp.x11.protocol.xproto.ListFontsWithInfoReply;
import com.github.moaxcp.x11.protocol.xproto.ListInstalledColormaps;
import com.github.moaxcp.x11.protocol.xproto.ListInstalledColormapsReply;
import com.github.moaxcp.x11.protocol.xproto.LookupColor;
import com.github.moaxcp.x11.protocol.xproto.LookupColorReply;
import com.github.moaxcp.x11.protocol.xproto.OpenFont;
import com.github.moaxcp.x11.protocol.xproto.QueryColors;
import com.github.moaxcp.x11.protocol.xproto.QueryColorsReply;
import com.github.moaxcp.x11.protocol.xproto.QueryFont;
import com.github.moaxcp.x11.protocol.xproto.QueryFontReply;
import com.github.moaxcp.x11.protocol.xproto.QueryTextExtents;
import com.github.moaxcp.x11.protocol.xproto.QueryTextExtentsReply;
import com.github.moaxcp.x11.protocol.xproto.RecolorCursor;
import com.github.moaxcp.x11.protocol.xproto.Rectangle;
import com.github.moaxcp.x11.protocol.xproto.Rgb;
import com.github.moaxcp.x11.protocol.xproto.SetClipRectangles;
import com.github.moaxcp.x11.protocol.xproto.SetDashes;
import com.github.moaxcp.x11.protocol.xproto.SetFontPath;
import com.github.moaxcp.x11.protocol.xproto.StoreColors;
import com.github.moaxcp.x11.protocol.xproto.StoreNamedColor;
import com.github.moaxcp.x11.protocol.xproto.Str;
import com.github.moaxcp.x11.protocol.xproto.UninstallColormap;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.client.ClientConnection;
import cn.oyzh.fx.xserver.client.ProtocolException;
import cn.oyzh.fx.xserver.display.Screen;
import cn.oyzh.fx.xserver.display.VisualInfo;
import cn.oyzh.fx.xserver.resource.BitmapFont;
import cn.oyzh.fx.xserver.resource.XColormap;
import cn.oyzh.fx.xserver.resource.XCursor;
import cn.oyzh.fx.xserver.resource.XFont;
import cn.oyzh.fx.xserver.resource.XGC;
import cn.oyzh.fx.xserver.resource.XPixmap;
import cn.oyzh.fx.xserver.resource.XResource;
import cn.oyzh.fx.xserver.resource.XWindow;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.primitive.ByteLists;
import org.eclipse.collections.impl.factory.primitive.IntLists;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The resource requests: graphics contexts, pixmaps, fonts, cursors and colormaps.
 *
 * <p>These are grouped together because they share one job: naming. Each of them takes an id the
 * <em>client</em> picked, validates it, and either publishes a new resource under it or resolves an
 * existing one. Everything they hand out is then consumed by the drawing requests, which live in
 * their own handler; nothing here touches the framebuffer.
 *
 * <p>Two deliberate simplifications run through the class. Fonts are Java fonts keyed by their X
 * name rather than rasterized X font files, and the colour database is the small built-in table
 * below rather than {@code rgb.txt}. Both are documented where they are used so a client that
 * depends on the missing fidelity is easy to spot.
 */
public final class ResourceHandlers {

    /** Largest pixmap the server will allocate, about 64 MB of pixels. */
    private static final long MAX_PIXMAP_PIXELS = 16L * 1024 * 1024;

    /** Every bit defined by the Gc value mask; anything else in a mask is a BadValue. */
    private static final int GC_VALID_MASK = 0x7FFFFF;

    /** Coloritem/StoreNamedColor flags: DoRed, DoGreen, DoBlue. */
    private static final int COLOR_VALID_FLAGS = 0b111;

    /** Core protocol {@code LeftToRight} draw direction. */
    private static final byte DRAW_DIRECTION_LTR = 0;

    /** Reported by {@code GetFontPath} until a client installs a path of its own. */
    private static final List<String> DEFAULT_FONT_PATH = List.of("built-ins");

    /**
     * The fonts the server can serve, in match order: a pattern that several names match resolves
     * to the first of them, which is what {@code XLoadQueryFont} relies on when it asks for one
     * name. Only the entries actually listed here exist - the font path below is informational.
     */
    private static final List<String> SERVER_FONTS = buildServerFonts();

    /**
     * The names the server lists, which are the built-in aliases plus each table font's own XLFD
     * name - the name its {@code ListFontsWithInfo} reply carries. A client that lists a font and
     * then asks about the name it was handed has to be able to find it, and Xlib's font-set
     * machinery is exactly that client: without the XLFD names here it was told "no such font" one
     * request after being told the font's name, and every charset came back missing (backlog C10).
     */
    private static List<String> buildServerFonts() {
        List<String> names = new ArrayList<>(List.of(
            "fixed",
            "6x13", "6x13bold", "7x13", "7x13bold", "8x13", "8x13bold", "9x15", "9x15bold",
            "10x20", "12x24", "nil", "nil2",
            "-misc-fixed-medium-r-normal--13-120-75-75-c-70-iso10646-1",
            "-misc-fixed-medium-r-normal--14-130-75-75-c-70-iso10646-1",
            "-misc-fixed-medium-r-semicondensed--13-120-75-75-c-60-iso10646-1",
            "-misc-fixed-bold-r-normal--13-120-75-75-c-70-iso10646-1",
            "-adobe-helvetica-medium-r-normal--12-120-75-75-p-67-iso8859-1",
            "-adobe-helvetica-bold-r-normal--12-120-75-75-p-70-iso8859-1",
            "-adobe-helvetica-medium-o-normal--12-120-75-75-p-67-iso8859-1",
            "-adobe-times-medium-r-normal--12-120-75-75-p-64-iso8859-1",
            "-adobe-times-bold-r-normal--12-120-75-75-p-67-iso8859-1",
            "-adobe-times-medium-i-normal--12-120-75-75-p-64-iso8859-1",
            "-adobe-courier-medium-r-normal--12-120-75-75-m-70-iso8859-1",
            "-adobe-courier-bold-r-normal--12-120-75-75-m-70-iso8859-1",
            "helvetica", "helvetica-bold", "helvetica-oblique",
            "times", "times-bold", "times-italic",
            "courier", "courier-bold", "courier-oblique",
            "symbol", "lucida", "lucida-bold", "new century schoolbook",
            "terminal", "clean", "micro", "vga", "screen"));
        names.addAll(cn.oyzh.fx.xserver.resource.BitmapFont.names());
        return List.copyOf(names);
    }

    /**
     * The colour database, as 0xRRGGBB. This is a curated subset of {@code rgb.txt}: the names
     * every toolkit actually looks up, plus the full {@code gray}/{@code grey} ramp generated
     * below. Names outside it, and the {@code #rgb} forms, are handled by
     * {@link #lookupColorName(String)}.
     */
    private static final Map<String, Integer> COLOR_NAMES = buildColorNames();

    private final XServer server;

    /** Colormaps clients installed explicitly, on top of each screen's default one. */
    private final List<XColormap> installedColormaps = new ArrayList<>();

    /**
     * The directories a font name is resolved in, and the files in them. A name the built-in tables
     * do not have is looked up here before it is approximated, which is the half of D3 that makes a
     * {@code SetFontPath} mean something.
     */
    private final cn.oyzh.fx.xserver.resource.FontPath fontPath =
            new cn.oyzh.fx.xserver.resource.FontPath(
                    java.nio.file.Path.of(System.getProperty("user.home", "."), ".xserver", "fonts")
                            .toString());

    public ResourceHandlers(XServer server) {
        this.server = server;
    }

    // ------------------------------------------------------------------
    // Graphics contexts
    // ------------------------------------------------------------------

    public void createGc(ClientConnection client, CreateGC r) {
        checkNewId(client, r.getCid());
        XResource drawable = server.resources().requireDrawable(client, r.getDrawable());
        // The GC is published only once every value has been accepted, so a rejected request
        // never leaves a half configured context behind for the id to name.
        XGC gc = new XGC(r.getCid(), client, screenOf(drawable), visualOf(drawable));
        applyGcValues(client, gc, depthOf(drawable), GcValues.of(r));
        server.resources().add(gc);
    }

    public void changeGc(ClientConnection client, ChangeGC r) {
        XGC gc = requireGc(client, r.getGc());
        // ChangeGC carries no drawable, so the depth a tile has to match is unknown here; the
        // depth 1 checks on stipple and clip mask still apply.
        applyGcValues(client, gc, -1, GcValues.of(r));
    }

    public void copyGc(ClientConnection client, CopyGC r) {
        int mask = r.getValueMask();
        if ((mask & ~GC_VALID_MASK) != 0) {
            throw ProtocolException.badValue(mask);
        }
        XGC src = requireGc(client, r.getSrcGc());
        XGC dst = requireGc(client, r.getDstGc());
        if (src.screen() != dst.screen()) {
            throw ProtocolException.badMatch();
        }
        if (Gc.FUNCTION.isEnabled(mask)) {
            dst.setFunction(src.function());
        }
        if (Gc.PLANE_MASK.isEnabled(mask)) {
            dst.setPlaneMask(src.planeMask());
        }
        if (Gc.FOREGROUND.isEnabled(mask)) {
            dst.setForeground(src.foreground());
        }
        if (Gc.BACKGROUND.isEnabled(mask)) {
            dst.setBackground(src.background());
        }
        if (Gc.LINE_WIDTH.isEnabled(mask)) {
            dst.setLineWidth(src.lineWidth());
        }
        if (Gc.LINE_STYLE.isEnabled(mask)) {
            dst.setLineStyle(src.lineStyle());
        }
        if (Gc.CAP_STYLE.isEnabled(mask)) {
            dst.setCapStyle(src.capStyle());
        }
        if (Gc.JOIN_STYLE.isEnabled(mask)) {
            dst.setJoinStyle(src.joinStyle());
        }
        if (Gc.FILL_STYLE.isEnabled(mask)) {
            dst.setFillStyle(src.fillStyle());
        }
        if (Gc.FILL_RULE.isEnabled(mask)) {
            dst.setFillRule(src.fillRule());
        }
        if (Gc.TILE.isEnabled(mask)) {
            dst.setTile(src.tile());
        }
        if (Gc.STIPPLE.isEnabled(mask)) {
            dst.setStipple(src.stipple());
        }
        if (Gc.TILE_STIPPLE_ORIGIN_X.isEnabled(mask)) {
            dst.setTileStippleOriginX(src.tileStippleOriginX());
        }
        if (Gc.TILE_STIPPLE_ORIGIN_Y.isEnabled(mask)) {
            dst.setTileStippleOriginY(src.tileStippleOriginY());
        }
        if (Gc.FONT.isEnabled(mask)) {
            dst.setFont(src.font());
        }
        if (Gc.SUBWINDOW_MODE.isEnabled(mask)) {
            dst.setSubwindowMode(src.subwindowMode());
        }
        if (Gc.GRAPHICS_EXPOSURES.isEnabled(mask)) {
            dst.setGraphicsExposures(src.graphicsExposures());
        }
        if (Gc.CLIP_ORIGIN_X.isEnabled(mask)) {
            dst.setClipOriginX(src.clipOriginX());
        }
        if (Gc.CLIP_ORIGIN_Y.isEnabled(mask)) {
            dst.setClipOriginY(src.clipOriginY());
        }
        if (Gc.CLIP_MASK.isEnabled(mask)) {
            // Null is None, which is the state a context starts in.
            dst.setClipMask(src.clipMask());
        }
        if (Gc.DASH_OFFSET.isEnabled(mask)) {
            dst.setDashOffset(src.dashOffset());
        }
        if (Gc.DASH_LIST.isEnabled(mask)) {
            dst.setDashes(src.dashes());
        }
        if (Gc.ARC_MODE.isEnabled(mask)) {
            dst.setArcMode(src.arcMode());
        }
    }

    public void setDashes(ClientConnection client, SetDashes r) {
        XGC gc = requireGc(client, r.getGc());
        if ((r.getDashes().size() & 1) != 0 || r.getDashes().isEmpty()) {
            // The dash list is a sequence of even-length gaps and dashes.
            throw ProtocolException.badValue(r.getDashes().size());
        }
        gc.setDashes(r.getDashes().toArray());
        gc.setDashOffset(Short.toUnsignedInt(r.getDashOffset()));
    }

    public void setClipRectangles(ClientConnection client, SetClipRectangles r) {
        XGC gc = requireGc(client, r.getGc());
        int ordering = r.getOrdering() & 0xFF;
        if (ordering > 3) {
            throw ProtocolException.badValue(ordering);
        }
        List<XGC.Rectangle2> rectangles = new ArrayList<>();
        for (Rectangle rectangle : r.getRectangles()) {
            rectangles.add(new XGC.Rectangle2(rectangle.getX(), rectangle.getY(),
                    Short.toUnsignedInt(rectangle.getWidth()),
                    Short.toUnsignedInt(rectangle.getHeight())));
        }
        gc.setClipRectangles(rectangles, ordering);
        gc.setClipOriginX(r.getClipXOrigin());
        gc.setClipOriginY(r.getClipYOrigin());
    }

    public void freeGc(ClientConnection client, FreeGC r) {
        XGC gc = requireGc(client, r.getGc());
        server.resources().free(gc.id());
    }

    /**
     * Applies a GC value mask. Validation and resource lookup happen before the first mutation so
     * that a bad request leaves the context exactly as it was, which is what clients assume when
     * they batch a {@code ChangeGC} with other requests.
     *
     * @param gcDepth depth of the drawable the context was created against, or -1 when unknown, in
     *                which case the tile depth check is skipped
     */
    private void applyGcValues(ClientConnection client, XGC gc, int gcDepth, GcValues v) {
        int mask = v.mask();
        if ((mask & ~GC_VALID_MASK) != 0) {
            throw ProtocolException.badValue(mask);
        }
        if (v.lineWidth() < 0 || v.lineWidth() > 0xFFFF) {
            throw ProtocolException.badValue(v.lineWidth());
        }
        checkRange(v.function(), 0, 15);
        checkRange(v.lineStyle(), XGC.LINE_SOLID, XGC.LINE_DOUBLE_DASH);
        checkRange(v.capStyle(), XGC.CAP_NOT_LAST, XGC.CAP_PROJECTING);
        checkRange(v.joinStyle(), XGC.JOIN_MITER, XGC.JOIN_BEVEL);
        checkRange(v.fillStyle(), XGC.FILL_SOLID, XGC.FILL_OPAQUE_STIPPLED);
        checkRange(v.fillRule(), XGC.FILL_EVEN_ODD, XGC.FILL_WINDING);
        checkRange(v.subwindowMode(), 0, 1);
        if (v.graphicsExposures() != 0 && v.graphicsExposures() != 1) {
            throw ProtocolException.badValue(v.graphicsExposures());
        }
        if (v.arcMode() < 0 || v.arcMode() > 1) {
            throw ProtocolException.badValue(v.arcMode());
        }

        XPixmap tile = Gc.TILE.isEnabled(mask)
                ? requirePixmap(client, v.tile(), true) : null;
        XPixmap stipple = Gc.STIPPLE.isEnabled(mask)
                ? requirePixmap(client, v.stipple(), true) : null;
        XPixmap clipMask = Gc.CLIP_MASK.isEnabled(mask)
                ? requirePixmap(client, v.clipMask(), true) : null;
        XFont font = Gc.FONT.isEnabled(mask) ? requireFont(client, v.font()) : null;

        if (tile != null && gcDepth >= 0 && tile.depth() != gcDepth) {
            throw ProtocolException.badMatch();
        }
        if (stipple != null && stipple.depth() != 1) {
            throw ProtocolException.badMatch();
        }
        if (clipMask != null && clipMask.depth() != 1) {
            throw ProtocolException.badMatch();
        }

        if (Gc.FUNCTION.isEnabled(mask)) {
            gc.setFunction(v.function());
        }
        if (Gc.PLANE_MASK.isEnabled(mask)) {
            gc.setPlaneMask(v.planeMask());
        }
        if (Gc.FOREGROUND.isEnabled(mask)) {
            gc.setForeground(v.foreground());
        }
        if (Gc.BACKGROUND.isEnabled(mask)) {
            gc.setBackground(v.background());
        }
        if (Gc.LINE_WIDTH.isEnabled(mask)) {
            gc.setLineWidth(v.lineWidth());
        }
        if (Gc.LINE_STYLE.isEnabled(mask)) {
            gc.setLineStyle(v.lineStyle());
        }
        if (Gc.CAP_STYLE.isEnabled(mask)) {
            gc.setCapStyle(v.capStyle());
        }
        if (Gc.JOIN_STYLE.isEnabled(mask)) {
            gc.setJoinStyle(v.joinStyle());
        }
        if (Gc.FILL_STYLE.isEnabled(mask)) {
            gc.setFillStyle(v.fillStyle());
        }
        if (Gc.FILL_RULE.isEnabled(mask)) {
            gc.setFillRule(v.fillRule());
        }
        if (Gc.TILE.isEnabled(mask)) {
            gc.setTile(tile);
        }
        if (Gc.STIPPLE.isEnabled(mask)) {
            gc.setStipple(stipple);
        }
        if (Gc.TILE_STIPPLE_ORIGIN_X.isEnabled(mask)) {
            gc.setTileStippleOriginX(v.tileStippleXOrigin());
        }
        if (Gc.TILE_STIPPLE_ORIGIN_Y.isEnabled(mask)) {
            gc.setTileStippleOriginY(v.tileStippleYOrigin());
        }
        if (Gc.FONT.isEnabled(mask)) {
            gc.setFont(font);
        }
        if (Gc.SUBWINDOW_MODE.isEnabled(mask)) {
            gc.setSubwindowMode(v.subwindowMode());
        }
        if (Gc.GRAPHICS_EXPOSURES.isEnabled(mask)) {
            gc.setGraphicsExposures(v.graphicsExposures() != 0);
        }
        if (Gc.CLIP_ORIGIN_X.isEnabled(mask)) {
            gc.setClipOriginX(v.clipXOrigin());
        }
        if (Gc.CLIP_ORIGIN_Y.isEnabled(mask)) {
            gc.setClipOriginY(v.clipYOrigin());
        }
        if (Gc.CLIP_MASK.isEnabled(mask)) {
            gc.setClipMask(clipMask);
        }
        if (Gc.DASH_OFFSET.isEnabled(mask)) {
            gc.setDashOffset(v.dashOffset());
        }
        // DashList is only meaningful on the wire in ChangeGC, where the list follows the fixed
        // fields; SetDashes is what clients use and what sets the list.
        if (Gc.ARC_MODE.isEnabled(mask)) {
            gc.setArcMode(v.arcMode());
        }
    }

    /**
     * The value mask and values of {@code CreateGC} and {@code ChangeGC}, which are the same
     * layout behind two different request classes.
     */
    private record GcValues(int mask, int function, int planeMask, int foreground, int background,
                            int lineWidth, int lineStyle, int capStyle, int joinStyle, int fillStyle,
                            int fillRule, int tile, int stipple, int tileStippleXOrigin,
                            int tileStippleYOrigin, int font, int subwindowMode,
                            int graphicsExposures, int clipXOrigin, int clipYOrigin, int clipMask,
                            int dashOffset, int arcMode) {

        static GcValues of(CreateGC r) {
            return new GcValues(r.getValueMask(), r.getFunction(), r.getPlaneMask(), r.getForeground(),
                    r.getBackground(), r.getLineWidth(), r.getLineStyle(), r.getCapStyle(),
                    r.getJoinStyle(), r.getFillStyle(), r.getFillRule(), r.getTile(), r.getStipple(),
                    r.getTileStippleXOrigin(), r.getTileStippleYOrigin(), r.getFont(),
                    r.getSubwindowMode(), r.getGraphicsExposures(), r.getClipXOrigin(),
                    r.getClipYOrigin(), r.getClipMask(), r.getDashOffset(), r.getArcMode());
        }

        static GcValues of(ChangeGC r) {
            return new GcValues(r.getValueMask(), r.getFunction(), r.getPlaneMask(), r.getForeground(),
                    r.getBackground(), r.getLineWidth(), r.getLineStyle(), r.getCapStyle(),
                    r.getJoinStyle(), r.getFillStyle(), r.getFillRule(), r.getTile(), r.getStipple(),
                    r.getTileStippleXOrigin(), r.getTileStippleYOrigin(), r.getFont(),
                    r.getSubwindowMode(), r.getGraphicsExposures(), r.getClipXOrigin(),
                    r.getClipYOrigin(), r.getClipMask(), r.getDashOffset(), r.getArcMode());
        }
    }

    // ------------------------------------------------------------------
    // Pixmaps
    // ------------------------------------------------------------------

    public void createPixmap(ClientConnection client, CreatePixmap r) {
        checkNewId(client, r.getPid());
        XResource drawable = server.resources().requireDrawable(client, r.getDrawable());
        int depth = r.getDepth() & 0xFF;
        int width = Short.toUnsignedInt(r.getWidth());
        int height = Short.toUnsignedInt(r.getHeight());
        if (width == 0 || height == 0) {
            throw ProtocolException.badValue(width == 0 ? width : height);
        }
        // Anything the screen has no format for would be undrawable, but depth 1 and 8 pixmaps
        // are made for stipples and masks even when no visual advertises them, so the check is
        // the protocol's depth range rather than the visual list.
        if (depth == 0 || depth > 32) {
            throw ProtocolException.badValue(depth);
        }
        // Every pixmap owns its pixels, so an absurd size is an allocation failure rather than a
        // protocol error - and an OutOfMemoryError here would drop the connection with no
        // explanation. The limit is generous: 4096x4096 is far more than any client needs.
        if ((long) width * height > MAX_PIXMAP_PIXELS) {
            throw ProtocolException.badAlloc();
        }
        Screen screen = screenOf(drawable);
        XPixmap pixmap = new XPixmap(r.getPid(), client, screen, width, height, depth,
                screen.defaultVisualForDepth(depth));
        server.resources().add(pixmap);
    }

    public void freePixmap(ClientConnection client, FreePixmap r) {
        XPixmap pixmap = requirePixmap(client, r.getPixmap(), false);
        server.resources().free(pixmap.id());
    }

    // ------------------------------------------------------------------
    // Fonts
    // ------------------------------------------------------------------

    public void openFont(ClientConnection client, OpenFont r) {
        checkNewId(client, r.getFid());
        String name = ascii(r.getName()).trim();
        if (name.isEmpty() || name.length() > 255) {
            throw ProtocolException.badName();
        }
        // The built-in tables first - they are the fonts this server always has, whatever the path
        // says - then the font path, and only then the Java approximation, which exists so that a
        // client that asks for a font nobody here has still gets something it can draw with.
        cn.oyzh.fx.xserver.resource.BitmapFont.Font fromFile = fontPath.open(name);
        XFont font = new XFont(r.getFid(), client, name, fromFile);
        server.resources().add(font);
    }

    public void closeFont(ClientConnection client, CloseFont r) {
        XFont font = requireFont(client, r.getFont());
        server.resources().free(font.id());
    }

    public void queryFont(ClientConnection client, QueryFont r) {
        XFont font = requireFont(client, r.getFont());
        client.sendReply(QueryFontReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .minBounds(bounds(font, true))
                .maxBounds(bounds(font, false))
                .minCharOrByte2((short) font.minChar())
                .maxCharOrByte2((short) font.maxChar())
                .defaultChar((short) font.defaultChar())
                .drawDirection(DRAW_DIRECTION_LTR)
                .minByte1((byte) 0)
                .maxByte1((byte) 0)
                .allCharsExist(false)
                .fontAscent((short) font.ascent())
                .fontDescent((short) font.descent())
                .properties(properties(font))
                .charInfos(charInfos(font))
                .build());
    }

    public void queryTextExtents(ClientConnection client, QueryTextExtents r) {
        XFont font = requireFont(client, r.getFont());
        org.eclipse.collections.api.list.ImmutableList<Char2b> chars = r.getString();
        int end = chars.size() - charLength(chars);
        int width = 0;
        // The left edge starts at "nothing seen yet" rather than at zero: it is the minimum of the
        // characters' left bearings, and a font whose smallest is one pixel (9x15, whose space sits
        // a pixel in) has a string that starts one pixel right of the origin. Starting at zero
        // reports a left edge no character in the string has.
        int left = Integer.MAX_VALUE;
        int right = 0;
        int ascent = 0;
        int descent = 0;
        for (int i = 0; i < end; i++) {
            int code = charCode(chars.get(i));
            BitmapFont.Metric metric = font.charMetric(code);
            if (metric != null) {
                // A real font's answer, measured against a reference server: the left edge is the
                // minimum of the characters' left bearings, the right edge the maximum of their
                // right bearings - both relative to the origin, which is why the right one grows
                // with the string and is not the last glyph's bearing. The ascent and descent are
                // the string's own maxima, not the font's: "Hello" is nine pixels tall where the
                // font is eleven, and a client that reserves the font's height for every line
                // would lay it out differently from one that reserves the reference's.
                left = Math.min(left, width + metric.leftBearing());
                right = Math.max(right, width + metric.rightBearing());
                ascent = Math.max(ascent, metric.ascent());
                descent = Math.max(descent, metric.descent());
            } else {
                // The Java approximation draws every glyph in its own cell and says so.
                left = Math.min(left, 0);
                right = Math.max(right, width + font.charWidth(code));
                ascent = Math.max(ascent, font.ascent());
                descent = Math.max(descent, font.descent());
            }
            width += font.charWidth(code);
        }
        if (end == 0) {
            // An empty string has no characters to take the metrics from, and the font's own are
            // what a client asking about nothing gets.
            left = 0;
            ascent = font.ascent();
            descent = font.descent();
        }
        client.sendReply(QueryTextExtentsReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .drawDirection(DRAW_DIRECTION_LTR)
                .fontAscent((short) font.ascent())
                .fontDescent((short) font.descent())
                .overallAscent((short) ascent)
                .overallDescent((short) descent)
                .overallWidth(width)
                .overallLeft(left)
                .overallRight(right)
                .build());
    }

    public void listFonts(ClientConnection client, ListFonts r) {
        List<Str> names = new ArrayList<>();
        for (String name : matchFonts(ascii(r.getPattern()), Short.toUnsignedInt(r.getMaxNames()))) {
            names.add(str(name));
        }
        client.sendReply(ListFontsReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .names(Lists.immutable.withAll(names))
                .build());
    }

    /**
     * {@code ListFontsWithInfo} answers with a reply per matching font and then a terminator whose
     * name is empty. All of them carry the sequence number of the one request, and the client
     * keeps reading until it sees the empty name.
     */
    public void listFontsWithInfo(ClientConnection client, ListFontsWithInfo r) {
        List<String> names = matchFonts(ascii(r.getPattern()), Short.toUnsignedInt(r.getMaxNames()));
        List<XFont> fonts = new ArrayList<>();
        List<ImmutableList<Fontprop>> fontProperties = new ArrayList<>();
        List<String> replyNames = new ArrayList<>();
        for (String name : names) {
            // Never registered: the font only exists to measure the one this reply describes.
            XFont font = new XFont(0, null, name);
            fonts.add(font);
            fontProperties.add(properties(font));
            // The font's own name, not the one the pattern matched: an alias is how a font is
            // *found* and the XLFD is what it *is*, and only the latter tells a client the charset
            // (see XFont.realName, and backlog C10 for what it costs to get this wrong).
            replyNames.add(font.realName());
        }
        // repliesHint is the byte size of what is still to come, starting with the terminator,
        // and is the only thing a client has to size its buffer ahead of the replies.
        int[] hints = new int[fonts.size()];
        int remaining = 32;
        for (int i = fonts.size() - 1; i >= 0; i--) {
            hints[i] = remaining;
            remaining += replySize(replyNames.get(i).length(), fontProperties.get(i).size());
        }
        for (int i = 0; i < fonts.size(); i++) {
            XFont font = fonts.get(i);
            client.sendReply(ListFontsWithInfoReply.builder()
                    .sequenceNumber(client.sequenceNumber())
                    .minBounds(bounds(font, true))
                    .maxBounds(bounds(font, false))
                    .minCharOrByte2((short) 0)
                    .maxCharOrByte2((short) 255)
                    .defaultChar((short) font.defaultChar())
                    .drawDirection(DRAW_DIRECTION_LTR)
                    .minByte1((byte) 0)
                    .maxByte1((byte) 0)
                    .allCharsExist(false)
                    .fontAscent((short) font.ascent())
                    .fontDescent((short) font.descent())
                    .repliesHint(hints[i])
                    .properties(fontProperties.get(i))
                    .name(str(replyNames.get(i)).getName())
                    .build());
        }
        client.sendReply(ListFontsWithInfoReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .minBounds(zeroBounds())
                .maxBounds(zeroBounds())
                .minCharOrByte2((short) 0)
                .maxCharOrByte2((short) 0)
                .defaultChar((short) 0)
                .drawDirection(DRAW_DIRECTION_LTR)
                .minByte1((byte) 0)
                .maxByte1((byte) 0)
                .allCharsExist(false)
                .fontAscent((short) 0)
                .fontDescent((short) 0)
                .repliesHint(0)
                .properties(Lists.immutable.empty())
                .name(ByteLists.immutable.empty())
                .build());
    }

    /**
     * Stores the path for {@code GetFontPath} to report and nothing else: fonts are resolved from
     * their names through the Java font machinery, so there is no directory to search.
     */
    public void setFontPath(ClientConnection client, SetFontPath r) {
        if (r.getFont().isEmpty()) {
            throw ProtocolException.badValue(0);
        }
        List<String> path = new ArrayList<>();
        for (Str entry : r.getFont()) {
            String element = ascii(entry.getName()).trim();
            if (element.isEmpty() || element.length() > 255) {
                throw ProtocolException.badValue(element.length());
            }
            path.add(element);
        }
        fontPath.setClientPath(path);
    }

    public void getFontPath(ClientConnection client, GetFontPath r) {
        List<Str> path = new ArrayList<>();
        for (String element : fontPath.elements()) {
            path.add(str(element));
        }
        client.sendReply(GetFontPathReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .path(Lists.immutable.withAll(path))
                .build());
    }

    /**
     * The fonts whose names match {@code pattern}, at most {@code maxNames} of them. Matching is
     * case insensitive, as X font names are, with {@code *} standing for any run of characters and
     * {@code ?} for exactly one.
     */
    private static List<String> matchFonts(String pattern, int maxNames) {
        List<String> matches = new ArrayList<>();
        for (String name : SERVER_FONTS) {
            if (matches.size() >= maxNames) {
                break;
            }
            if (matches(pattern, name)) {
                matches.add(name);
            }
        }
        return matches;
    }

    private static boolean matches(String pattern, String name) {
        return matches(pattern, 0, name, 0);
    }

    private static boolean matches(String pattern, int p, String name, int n) {
        while (p < pattern.length()) {
            char c = pattern.charAt(p);
            if (c == '*') {
                while (p < pattern.length() && pattern.charAt(p) == '*') {
                    p++;
                }
                if (p == pattern.length()) {
                    return true;
                }
                for (int i = n; i <= name.length(); i++) {
                    if (matches(pattern, p, name, i)) {
                        return true;
                    }
                }
                return false;
            }
            if (n >= name.length()) {
                return false;
            }
            if (c != '?' && Character.toLowerCase(c) != Character.toLowerCase(name.charAt(n))) {
                return false;
            }
            p++;
            n++;
        }
        return n == name.length();
    }

    /** The font's property set, with each property name interned as an atom. */
    private ImmutableList<Fontprop> properties(XFont font) {
        List<Fontprop> properties = new ArrayList<>();
        BitmapFont.Font bitmap = font.bitmap();
        if (bitmap != null) {
            // The font's own properties, in the order the font gives them, with the string valued
            // ones interned as atoms the way {@code QueryFont} carries them. The order is not a
            // decision a client can act on - it looks properties up by name - but it is the
            // reference's, and this reply is compared with the reference's field by field.
            for (int i = 0; i < bitmap.propertyNames().length; i++) {
                String text = bitmap.propertyStrings()[i];
                properties.add(Fontprop.builder()
                        .name(server.atoms().intern(bitmap.propertyNames()[i]))
                        .value(text == null ? bitmap.propertyValues()[i]
                                : server.atoms().intern(text))
                        .build());
            }
            return Lists.immutable.withAll(properties);
        }
        for (XFont.FontProperty property : font.properties()) {
            properties.add(Fontprop.builder()
                    .name(server.atoms().intern(property.name()))
                    .value(property.value())
                    .build());
        }
        // String valued properties carry the atom of the string as their value.
        for (XFont.TextProperty property : font.textProperties()) {
            properties.add(Fontprop.builder()
                    .name(server.atoms().intern(property.name()))
                    .value(server.atoms().intern(property.value()))
                    .build());
        }
        return Lists.immutable.withAll(properties);
    }

    /** The foundry component of an XLFD name, or the catch-all one for short alias names. */
    private static String foundryOf(String fontName) {
        if (fontName.startsWith("-")) {
            String[] parts = fontName.split("-");
            if (parts.length > 1 && !parts[1].isEmpty()) {
                return parts[1];
            }
        }
        return "misc";
    }

    /**
     * Min-bounds and max-bounds, which for a font with a table are the font's own and are <em>not</em>
     * the extremes of its table: a character's width can equal the font's minimum and maximum at once
     * (a fixed-pitch font is all one width), so which bound is being asked for has to be said rather
     * than inferred from the width.
     */
    private static Charinfo bounds(XFont font, boolean minBounds) {
        BitmapFont.Metric real = font.bitmapBounds(minBounds);
        if (real != null) {
            return charinfo(real);
        }
        int characterWidth = minBounds ? font.minWidth() : font.maxWidth();
        return Charinfo.builder()
                .leftSideBearing((short) font.minLeftBearing())
                .rightSideBearing((short) font.maxRightBearing())
                .characterWidth((short) characterWidth)
                .ascent((short) font.ascent())
                .descent((short) font.descent())
                .attributes((short) 0)
                .build();
    }

    private static Charinfo zeroBounds() {
        return Charinfo.builder()
                .leftSideBearing((short) 0)
                .rightSideBearing((short) 0)
                .characterWidth((short) 0)
                .ascent((short) 0)
                .descent((short) 0)
                .attributes((short) 0)
                .build();
    }

    /**
     * Per-character metrics for the whole byte range. Clients use these for proportional fonts and
     * for laying out text before drawing it, so they are reported rather than left for the client
     * to guess from the bounds.
     */
    private static ImmutableList<Charinfo> charInfos(XFont font) {
        // The reply's table covers the font's own range of codes and no more, and a client indexes
        // it from min-char-or-byte2: 255 entries starting at one is what a reference server answers
        // for a font whose codes begin there.
        List<Charinfo> infos = new ArrayList<>(font.maxChar() - font.minChar() + 1);
        for (int ch = font.minChar(); ch <= font.maxChar(); ch++) {
            // A font with a real table reports each character's own bearings: a client lays text out
            // from these, so "zero left bearing, right bearing at the advance" - which is what this
            // answered for a modelled font and is not true of any real one - mis-lays every string.
            BitmapFont.Metric metric = font.charMetric(ch);
            if (metric != null) {
                infos.add(charinfo(metric));
                continue;
            }
            int width = font.charWidth(ch);
            infos.add(Charinfo.builder()
                    .leftSideBearing((short) 0)
                    .rightSideBearing((short) width)
                    .characterWidth((short) width)
                    .ascent((short) font.ascent())
                    .descent((short) font.descent())
                    .attributes((short) 0)
                    .build());
        }
        return Lists.immutable.withAll(infos);
    }

    private static Charinfo charinfo(BitmapFont.Metric metric) {
        return Charinfo.builder()
                .leftSideBearing((short) metric.leftBearing())
                .rightSideBearing((short) metric.rightBearing())
                .characterWidth((short) metric.width())
                .ascent((short) metric.ascent())
                .descent((short) metric.descent())
                .attributes((short) metric.attributes())
                .build();
    }

    /**
     * {@code QueryTextExtents} pads an odd length string with one dummy {@code Char2b}, and the
     * generated decoder keeps it. An even sized list that ends in NUL is therefore the padded form
     * of an odd string, and the padding has to be left out of the measurement.
     */
    private static int charLength(org.eclipse.collections.api.list.ImmutableList<Char2b> chars) {
        if (chars.size() % 2 == 0 && !chars.isEmpty()) {
            Char2b last = chars.get(chars.size() - 1);
            if (last.getByte1() == 0 && last.getByte2() == 0) {
                return 1;
            }
        }
        return 0;
    }

    /**
     * The 16 bit character a {@code Char2b} names. Xlib sends Latin-1 text as {@code (0, char)},
     * but the two bytes are accepted either way round so a client that puts the character in the
     * high byte is measured correctly too.
     */
    private static int charCode(Char2b c) {
        int high = c.getByte1() & 0xFF;
        int low = c.getByte2() & 0xFF;
        if (high == 0) {
            return low;
        }
        if (low == 0) {
            return high;
        }
        return (high << 8) | low;
    }

    /** The size of a {@code ListFontsWithInfo} reply on the wire, padded to four bytes. */
    private static int replySize(int nameLength, int propertyCount) {
        return (32 + propertyCount * 8 + nameLength + 3) & ~3;
    }

    // ------------------------------------------------------------------
    // Cursors
    // ------------------------------------------------------------------

    /**
     * The foreground and background colours of the request are not used: {@link XCursor} renders
     * cursor bitmaps in the standard black/white form, which is what every toolkit's cursors are
     * drawn with anyway.
     *
     * <p><b>The hotspot is not required to be inside the pixmap.</b> A reference server accepts a
     * hotspot outside it - measured with a 1x1 source and hotspot (1,1), which Xvfb creates and this
     * server used to refuse with {@code BadMatch} - and a toolkit does exactly that: GTK builds a
     * cursor out of RENDER pictures and installs it by calling {@code CreateCursor} with a one pixel
     * placeholder source, a hotspot outside it, and the real image on the window through
     * {@code XRenderCreateCursor}. The refusal was not a cosmetic one: it arrived at the toolkit as an
     * X error, and GTK's default handler ends the process - which is why a forwarded application's
     * input fields lost their right-click menus, their clipboard items and their Ctrl+A (A22). The
     * hotspot is only an offset into the cursor image, so a hotspot outside it is a cursor whose image
     * hangs off its own origin, which is a perfectly drawable thing.
     */
    public void createCursor(ClientConnection client, CreateCursor r) {
        checkNewId(client, r.getCid());
        XPixmap source = requireCursorPixmap(client, r.getSource());
        XPixmap mask = requireCursorPixmap(client, r.getMask());
        if (source == null && mask == null) {
            throw ProtocolException.badMatch();
        }
        int width = source != null ? source.width() : mask.width();
        int height = source != null ? source.height() : mask.height();
        int hotspotX = Short.toUnsignedInt(r.getX());
        int hotspotY = Short.toUnsignedInt(r.getY());
        XCursor cursor = XCursor.fromBitmaps(r.getCid(), client, source, mask, 0, 0, 0, 0, width,
                height, hotspotX, hotspotY);
        server.resources().add(cursor);
    }

    public void createGlyphCursor(ClientConnection client, CreateGlyphCursor r) {
        checkNewId(client, r.getCid());
        XFont sourceFont = requireFont(client, r.getSourceFont());
        XFont maskFont = r.getMaskFont() == 0 ? null : requireFont(client, r.getMaskFont());
        // A mask font of None means the source glyph's own cell masks itself in.
        int maskChar = maskFont == null ? 0 : Short.toUnsignedInt(r.getMaskChar());
        XCursor cursor = XCursor.fromGlyph(r.getCid(), client, sourceFont,
                Short.toUnsignedInt(r.getSourceChar()), maskChar,
                argb(r.getForeRed(), r.getForeGreen(), r.getForeBlue()),
                argb(r.getBackRed(), r.getBackGreen(), r.getBackBlue()));
        server.resources().add(cursor);
    }

    public void freeCursor(ClientConnection client, FreeCursor r) {
        XCursor cursor = requireCursor(client, r.getCursor());
        server.resources().free(cursor.id());
    }

    public void recolorCursor(ClientConnection client, RecolorCursor r) {
        XCursor cursor = requireCursor(client, r.getCursor());
        if (!cursor.isRecolorable()) {
            // Only cursors built from a font glyph have colours to change; a bitmap cursor has its
            // colours burnt into its pixels.
            throw ProtocolException.badMatch();
        }
        cursor.setColors(argb(r.getForeRed(), r.getForeGreen(), r.getForeBlue()),
                argb(r.getBackRed(), r.getBackGreen(), r.getBackBlue()));
    }

    /** A cursor source or mask pixmap, or null when the field is None. */
    private XPixmap requireCursorPixmap(ClientConnection client, int id) {
        if (id == 0) {
            return null;
        }
        XResource resource = server.resources().requireDrawable(client, id);
        if (!(resource instanceof XPixmap pixmap)) {
            throw ProtocolException.badMatch();
        }
        if (pixmap.depth() != 1) {
            throw ProtocolException.badMatch();
        }
        return pixmap;
    }

    // ------------------------------------------------------------------
    // Colormaps
    // ------------------------------------------------------------------

    public void createColormap(ClientConnection client, CreateColormap r) {
        checkNewId(client, r.getMid());
        XWindow window = requireWindow(client, r.getWindow());
        VisualInfo visual = window.screen().visualById(r.getVisual());
        if (visual == null) {
            throw ProtocolException.badMatch();
        }
        int alloc = r.getAlloc() & 0xFF;
        if (alloc != XColormap.NONE && alloc != XColormap.ALL) {
            throw ProtocolException.badValue(alloc);
        }
        // AllocAll preallocates every cell, which only means something for the writable visual
        // classes. AllocNone is accepted for every visual: rejecting it on a TrueColor visual
        // would turn away every toolkit that makes a private colormap for an ARGB window.
        if (alloc == XColormap.ALL && visual.visualClass() != VisualInfo.PSEUDO_COLOR
                && visual.visualClass() != VisualInfo.DIRECT_COLOR) {
            throw ProtocolException.badMatch();
        }
        XColormap colormap = new XColormap(r.getMid(), client, window.screen(), visual,
                alloc == XColormap.ALL);
        server.resources().add(colormap);
    }

    public void freeColormap(ClientConnection client, FreeColormap r) {
        XColormap colormap = requireColormap(client, r.getCmap());
        if (colormap.id() == server.rootColormap().id()) {
            // The root window and every window without a colormap of its own depend on it.
            throw ProtocolException.badMatch();
        }
        installedColormaps.remove(colormap);
        server.resources().free(colormap.id());
    }

    public void copyColormapAndFree(ClientConnection client, CopyColormapAndFree r) {
        checkNewId(client, r.getMid());
        XColormap source = requireColormap(client, r.getSrcCmap());
        if (source.id() == server.rootColormap().id()) {
            throw ProtocolException.badMatch();
        }
        // Every cell is copied, allocated or not, because the colormap only exposes its values.
        XColormap copy = new XColormap(r.getMid(), client, source.screen(), source.visual(), false);
        for (int i = 0; i < source.size(); i++) {
            copy.storeColor(i, source.colorAt(i));
        }
        server.resources().add(copy);
        // The copy inherits the source's place in the installed list, and the source goes away.
        int installed = installedColormaps.indexOf(source);
        if (installed >= 0) {
            installedColormaps.set(installed, copy);
        }
        server.resources().free(source.id());
    }

    public void installColormap(ClientConnection client, InstallColormap r) {
        XColormap colormap = requireColormap(client, r.getCmap());
        if (!installedColormaps.contains(colormap)) {
            installedColormaps.add(colormap);
        }
    }

    public void uninstallColormap(ClientConnection client, UninstallColormap r) {
        XColormap colormap = requireColormap(client, r.getCmap());
        // The default colormap of a screen is always reported as installed whether it is in this
        // list or not, so removing it here is accepted and simply has no effect.
        installedColormaps.remove(colormap);
    }

    public void listInstalledColormaps(ClientConnection client, ListInstalledColormaps r) {
        XWindow window = requireWindow(client, r.getWindow());
        List<Integer> cmaps = new ArrayList<>();
        XColormap defaultColormap = window.screen().defaultColormap();
        if (defaultColormap != null) {
            cmaps.add(defaultColormap.id());
        }
        XColormap windowColormap = window.colormap();
        if (windowColormap != null && !cmaps.contains(windowColormap.id())) {
            cmaps.add(windowColormap.id());
        }
        for (XColormap colormap : installedColormaps) {
            if (colormap.screen() == window.screen() && !cmaps.contains(colormap.id())) {
                cmaps.add(colormap.id());
            }
        }
        int[] result = new int[cmaps.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = cmaps.get(i);
        }
        client.sendReply(ListInstalledColormapsReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .cmaps(IntLists.immutable.of(result))
                .build());
    }

    public void allocColor(ClientConnection client, AllocColor r) {
        XColormap colormap = requireColormap(client, r.getCmap());
        int red = to8(Short.toUnsignedInt(r.getRed()));
        int green = to8(Short.toUnsignedInt(r.getGreen()));
        int blue = to8(Short.toUnsignedInt(r.getBlue()));
        int pixel = colormap.allocColor(red, green, blue);
        client.sendReply(AllocColorReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .red(r.getRed())
                .green(r.getGreen())
                .blue(r.getBlue())
                .pixel(pixel)
                .build());
    }

    public void allocNamedColor(ClientConnection client, AllocNamedColor r) {
        XColormap colormap = requireColormap(client, r.getCmap());
        Color8 color = lookupColorName(ascii(r.getName()));
        if (color == null) {
            throw ProtocolException.badName();
        }
        int pixel = colormap.allocColor(color.red(), color.green(), color.blue());
        client.sendReply(AllocNamedColorReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .pixel(pixel)
                .exactRed((short) color.red16())
                .exactGreen((short) color.green16())
                .exactBlue((short) color.blue16())
                // The exact and visual values are the same: there is no hardware palette to snap
                // the requested colour to.
                .visualRed((short) color.red16())
                .visualGreen((short) color.green16())
                .visualBlue((short) color.blue16())
                .build());
    }

    public void allocColorCells(ClientConnection client, AllocColorCells r) {
        XColormap colormap = requireColormap(client, r.getCmap());
        int colors = Short.toUnsignedInt(r.getColors());
        int planes = Short.toUnsignedInt(r.getPlanes());
        if (colors == 0) {
            throw ProtocolException.badValue(colors);
        }
        if (planes > 32) {
            throw ProtocolException.badValue(planes);
        }
        int[] pixels = colormap.allocColorCells(r.isContiguous(), colors, planes);
        int[] masks = new int[planes];
        for (int i = 0; i < planes; i++) {
            masks[i] = 1 << i;
        }
        client.sendReply(AllocColorCellsReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .pixels(IntLists.immutable.of(pixels))
                .masks(IntLists.immutable.of(masks))
                .build());
    }

    public void allocColorPlanes(ClientConnection client, AllocColorPlanes r) {
        XColormap colormap = requireColormap(client, r.getCmap());
        int colors = Short.toUnsignedInt(r.getColors());
        int reds = Short.toUnsignedInt(r.getReds());
        int greens = Short.toUnsignedInt(r.getGreens());
        int blues = Short.toUnsignedInt(r.getBlues());
        if (colors == 0) {
            throw ProtocolException.badValue(colors);
        }
        if (reds == 0) {
            throw ProtocolException.badValue(reds);
        }
        if (greens == 0) {
            throw ProtocolException.badValue(greens);
        }
        if (blues == 0) {
            throw ProtocolException.badValue(blues);
        }
        if (reds + greens + blues > 32) {
            throw ProtocolException.badValue(reds + greens + blues);
        }
        // XColormap models at most four planes, but the cells it hands out do not depend on the
        // plane count, so the request is clamped there and the three colour masks are built from
        // the counts the client actually asked for.
        XColormap.PlaneAllocation allocation =
                colormap.allocColorPlanes(colors, Math.min(4, reds + greens + blues));
        client.sendReply(AllocColorPlanesReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .redMask(planeMask(0, reds))
                .greenMask(planeMask(reds, greens))
                .blueMask(planeMask(reds + greens, blues))
                .pixels(IntLists.immutable.of(allocation.pixels()))
                .build());
    }

    public void freeColors(ClientConnection client, FreeColors r) {
        XColormap colormap = requireColormap(client, r.getCmap());
        colormap.freeColors(r.getPixels().toArray());
    }

    public void storeColors(ClientConnection client, StoreColors r) {
        XColormap colormap = requireColormap(client, r.getCmap());
        for (Coloritem item : r.getItems()) {
            checkColorFlags(item.getFlags() & 0xFF);
        }
        if (colormap.visual() != null && colormap.visual().isTrueColor()) {
            // On a TrueColor visual a pixel is a packed colour, so there is no cell to write to;
            // the request is accepted and has no effect, as on a real server.
            return;
        }
        for (Coloritem item : r.getItems()) {
            checkPixel(colormap, item.getPixel());
        }
        for (Coloritem item : r.getItems()) {
            int argb = colormap.colorAt(item.getPixel());
            int red = item.isFlagsEnabled(ColorFlag.RED)
                    ? to8(Short.toUnsignedInt(item.getRed())) : (argb >> 16) & 0xFF;
            int green = item.isFlagsEnabled(ColorFlag.GREEN)
                    ? to8(Short.toUnsignedInt(item.getGreen())) : (argb >> 8) & 0xFF;
            int blue = item.isFlagsEnabled(ColorFlag.BLUE)
                    ? to8(Short.toUnsignedInt(item.getBlue())) : argb & 0xFF;
            colormap.storeColor(item.getPixel(), 0xFF000000 | (red << 16) | (green << 8) | blue);
        }
    }

    public void storeNamedColor(ClientConnection client, StoreNamedColor r) {
        int flags = r.getFlags() & 0xFF;
        checkColorFlags(flags);
        XColormap colormap = requireColormap(client, r.getCmap());
        Color8 color = lookupColorName(ascii(r.getName()));
        if (color == null) {
            throw ProtocolException.badName();
        }
        if (colormap.visual() != null && colormap.visual().isTrueColor()) {
            return;
        }
        checkPixel(colormap, r.getPixel());
        int argb = colormap.colorAt(r.getPixel());
        int red = (flags & ColorFlag.RED.getValue()) != 0 ? color.red() : (argb >> 16) & 0xFF;
        int green = (flags & ColorFlag.GREEN.getValue()) != 0 ? color.green() : (argb >> 8) & 0xFF;
        int blue = (flags & ColorFlag.BLUE.getValue()) != 0 ? color.blue() : argb & 0xFF;
        colormap.storeColor(r.getPixel(), 0xFF000000 | (red << 16) | (green << 8) | blue);
    }

    public void queryColors(ClientConnection client, QueryColors r) {
        XColormap colormap = requireColormap(client, r.getCmap());
        List<Rgb> colors = new ArrayList<>();
        for (int pixel : r.getPixels().toArray()) {
            int argb = colorForPixel(colormap, pixel);
            colors.add(Rgb.builder()
                    .red((short) to16((argb >> 16) & 0xFF))
                    .green((short) to16((argb >> 8) & 0xFF))
                    .blue((short) to16(argb & 0xFF))
                    .build());
        }
        client.sendReply(QueryColorsReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .colors(Lists.immutable.withAll(colors))
                .build());
    }

    public void lookupColor(ClientConnection client, LookupColor r) {
        // The colormap does not take part in the lookup: the name database is the same for every
        // colormap, and it is only named in the request so the reply is ordered with the rest.
        requireColormap(client, r.getCmap());
        Color8 color = lookupColorName(ascii(r.getName()));
        if (color == null) {
            throw ProtocolException.badName();
        }
        client.sendReply(LookupColorReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .exactRed((short) color.red16())
                .exactGreen((short) color.green16())
                .exactBlue((short) color.blue16())
                .visualRed((short) color.red16())
                .visualGreen((short) color.green16())
                .visualBlue((short) color.blue16())
                .build());
    }

    // ------------------------------------------------------------------
    // Colour names
    // ------------------------------------------------------------------

    /**
     * Resolves an X colour name. Supported forms are the {@code #RGB}, {@code #RRGGBB},
     * {@code #RRRGGGBBB} and {@code #RRRRGGGGBBBB} specifications and the built-in name table,
     * which holds the common colours together with the full {@code gray}/{@code grey} ramp.
     * Anything else, including the {@code rgb:} syntax that clients normally expand themselves, is
     * reported to the client as {@code BadName}.
     *
     * @return the colour, or null when the name is not known
     */
    private static Color8 lookupColorName(String name) {
        String key = name.trim().toLowerCase(Locale.ROOT);
        if (key.startsWith("#")) {
            return parseHexColor(key);
        }
        Integer packed = COLOR_NAMES.get(key);
        if (packed == null) {
            return null;
        }
        return new Color8((packed >> 16) & 0xFF, (packed >> 8) & 0xFF, packed & 0xFF);
    }

    /** The {@code #rgb} forms, with each channel scaled from 1, 2, 3 or 4 hex digits to 8 bits. */
    private static Color8 parseHexColor(String text) {
        String digits = text.substring(1);
        int perChannel = switch (digits.length()) {
            case 3, 6, 9, 12 -> digits.length() / 3;
            default -> 0;
        };
        if (perChannel == 0) {
            return null;
        }
        int[] channels = new int[3];
        int max = (1 << (4 * perChannel)) - 1;
        for (int i = 0; i < 3; i++) {
            int raw;
            try {
                raw = Integer.parseInt(digits, i * perChannel, (i + 1) * perChannel, 16);
            } catch (NumberFormatException e) {
                return null;
            }
            channels[i] = (raw * 255 + max / 2) / max;
        }
        return new Color8(channels[0], channels[1], channels[2]);
    }

    private static Map<String, Integer> buildColorNames() {
        Map<String, Integer> names = new HashMap<>();
        names.put("black", 0x000000);
        names.put("white", 0xFFFFFF);
        names.put("red", 0xFF0000);
        names.put("green", 0x00FF00);
        names.put("blue", 0x0000FF);
        names.put("yellow", 0xFFFF00);
        names.put("cyan", 0x00FFFF);
        names.put("magenta", 0xFF00FF);
        names.put("gray", 0xBEBEBE);
        names.put("grey", 0xBEBEBE);
        names.put("orange", 0xFFA500);
        names.put("pink", 0xFFC0CB);
        names.put("brown", 0xA52A2A);
        names.put("purple", 0xA020F0);
        names.put("violet", 0xEE82EE);
        names.put("navy", 0x000080);
        names.put("navyblue", 0x000080);
        names.put("gold", 0xFFD700);
        names.put("khaki", 0xF0E68C);
        names.put("salmon", 0xFA8072);
        names.put("snow", 0xFFFAFA);
        names.put("ivory", 0xFFFFF0);
        names.put("beige", 0xF5F5DC);
        names.put("wheat", 0xF5DEB3);
        names.put("tan", 0xD2B48C);
        names.put("coral", 0xFF7F50);
        names.put("tomato", 0xFF6347);
        names.put("crimson", 0xDC143C);
        names.put("firebrick", 0xB22222);
        names.put("indigo", 0x4B0082);
        names.put("orchid", 0xDA70D6);
        names.put("plum", 0xDDA0DD);
        names.put("thistle", 0xD8BFD8);
        names.put("peru", 0xCD853F);
        names.put("sienna", 0xA0522D);
        names.put("chocolate", 0xD2691E);
        names.put("seagreen", 0x2E8B57);
        names.put("forestgreen", 0x228B22);
        names.put("limegreen", 0x32CD32);
        names.put("palegreen", 0x98FB98);
        names.put("springgreen", 0x00FF7F);
        names.put("yellowgreen", 0x9ACD32);
        names.put("olivegreen", 0x6B8E23);
        names.put("goldenrod", 0xDAA520);
        names.put("indianred", 0xCD5C5C);
        names.put("mediumblue", 0x0000CD);
        names.put("royalblue", 0x4169E1);
        names.put("skyblue", 0x87CEEB);
        names.put("slateblue", 0x6A5ACD);
        names.put("steelblue", 0x4682B4);
        names.put("cadetblue", 0x5F9EA0);
        names.put("turquoise", 0x40E0D0);
        names.put("aquamarine", 0x7FFFD4);
        names.put("lavender", 0xE6E6FA);
        names.put("bisque", 0xFFE4C4);
        names.put("peachpuff", 0xFFDAB9);
        names.put("darkgray", 0xA9A9A9);
        names.put("darkgrey", 0xA9A9A9);
        names.put("lightgray", 0xD3D3D3);
        names.put("lightgrey", 0xD3D3D3);
        names.put("darkred", 0x8B0000);
        names.put("darkgreen", 0x006400);
        names.put("darkblue", 0x00008B);
        names.put("darkcyan", 0x008B8B);
        names.put("darkmagenta", 0x8B008B);
        names.put("darkorange", 0xFF8C00);
        names.put("darkviolet", 0x9400D3);
        names.put("maroon", 0xB03060);
        // The X11 grey ramp: grey0 is black, grey100 is white.
        for (int i = 0; i <= 100; i++) {
            int level = (i * 255 + 50) / 100;
            int packed = (level << 16) | (level << 8) | level;
            names.put("gray" + i, packed);
            names.put("grey" + i, packed);
        }
        // X accepts both spellings of grey everywhere, and clients use both - twm asks for
        // "slategrey", which a table listing only "slategray" would reject with BadName.
        for (Map.Entry<String, Integer> entry : new ArrayList<>(names.entrySet())) {
            if (entry.getKey().contains("gray")) {
                names.putIfAbsent(entry.getKey().replace("gray", "grey"), entry.getValue());
            }
        }
        return Map.copyOf(names);
    }

    /** An 8 bit per channel colour; the requests carry 16 bit channels. */
    private record Color8(int red, int green, int blue) {
        int red16() {
            return red * 0x101;
        }

        int green16() {
            return green * 0x101;
        }

        int blue16() {
            return blue * 0x101;
        }
    }

    /**
     * One channel of a protocol colour as the eight bits this server stores.
     *
     * <p>The top byte is taken rather than the value scaled: {@code XParseColor} turns an eight bit
     * component into a sixteen bit one by repeating it, so {@code #ff0000} arrives as {@code 0xff00}
     * and must come back out as {@code 0xff}. Rounding - which is what a scale would do - gives
     * {@code 0xfe} instead, and every named colour a client allocates is then a shade too dark.
     */
    private static int to8(int value16) {
        return (value16 >> 8) & 0xFF;
    }

    private static int to16(int value8) {
        return value8 * 0x101;
    }

    /**
     * A 16 bit per channel request colour packed into 0xAARRGGBB, for cursor colours. The channels
     * arrive as shorts, so they are widened unsigned: 0xFFFF is full intensity, not -1.
     */
    private static int argb(short red, short green, short blue) {
        return 0xFF000000 | (to8(Short.toUnsignedInt(red)) << 16)
                | (to8(Short.toUnsignedInt(green)) << 8) | to8(Short.toUnsignedInt(blue));
    }

    private static int planeMask(int offset, int planes) {
        return ((1 << planes) - 1) << offset;
    }

    // ------------------------------------------------------------------
    // Lookup and validation helpers
    // ------------------------------------------------------------------

    /**
     * Clients choose their own resource ids, so a new resource has to be given an id inside the
     * client's range that nothing is using yet.
     */
    private void checkNewId(ClientConnection client, int id) {
        if (!client.ownsIdRange(id)) {
            throw ProtocolException.badIdChoice(id);
        }
        if (server.resources().contains(id)) {
            throw ProtocolException.badIdChoice(id);
        }
    }

    private XGC requireGc(ClientConnection client, int id) {
        return (XGC) server.resources().require(client, id, XResource.CLASS_GC, false);
    }

    private XPixmap requirePixmap(ClientConnection client, int id, boolean allowNone) {
        return (XPixmap) server.resources().require(client, id, XResource.CLASS_PIXMAP, allowNone);
    }

    private XFont requireFont(ClientConnection client, int id) {
        return (XFont) server.resources().require(client, id, XResource.CLASS_FONT, false);
    }

    private XCursor requireCursor(ClientConnection client, int id) {
        return (XCursor) server.resources().require(client, id, XResource.CLASS_CURSOR, false);
    }

    private XColormap requireColormap(ClientConnection client, int id) {
        return (XColormap) server.resources().require(client, id, XResource.CLASS_COLORMAP, false);
    }

    private XWindow requireWindow(ClientConnection client, int id) {
        return (XWindow) server.resources().require(client, id, XResource.CLASS_WINDOW, false);
    }

    private static void checkRange(int value, int min, int max) {
        if (value < min || value > max) {
            throw ProtocolException.badValue(value);
        }
    }

    private static void checkColorFlags(int flags) {
        if ((flags & ~COLOR_VALID_FLAGS) != 0) {
            throw ProtocolException.badValue(flags);
        }
    }

    /**
     * Resolves a pixel to its colour. On a TrueColor visual a pixel is a packed RGB value rather
     * than a cell index, so any value in the visual's range is valid and is decomposed through the
     * visual's channel masks - which is what clients such as {@code xwd} rely on when they read
     * back a screenshot. On an indexed visual it really is an index and must be in range.
     */
    private static int colorForPixel(XColormap colormap, int pixel) {
        VisualInfo visual = colormap.visual();
        if (visual != null && visual.isTrueColor()) {
            return visual.toArgb(pixel);
        }
        checkPixel(colormap, pixel);
        return colormap.colorAt(pixel);
    }

    private static void checkPixel(XColormap colormap, int pixel) {
        if (pixel < 0 || pixel >= colormap.size()) {
            throw ProtocolException.badValue(pixel);
        }
    }

    private static Screen screenOf(XResource drawable) {
        if (drawable instanceof XWindow window) {
            return window.screen();
        }
        if (drawable instanceof XPixmap pixmap) {
            return pixmap.screen();
        }
        throw ProtocolException.badDrawable(drawable.id());
    }

    /** The visual of a drawable, which is what a GC created on it converts colours in (B5). */
    private static VisualInfo visualOf(XResource drawable) {
        if (drawable instanceof XWindow window) {
            return window.visual();
        }
        if (drawable instanceof XPixmap pixmap) {
            return pixmap.visual();
        }
        return null;
    }

    private static int depthOf(XResource drawable) {
        if (drawable instanceof XWindow window) {
            return window.depth();
        }
        if (drawable instanceof XPixmap pixmap) {
            return pixmap.depth();
        }
        return -1;
    }

    private static Str str(String name) {
        return Str.builder()
                .name(ByteLists.immutable.of(name.getBytes(StandardCharsets.ISO_8859_1)))
                .build();
    }

    private static String ascii(org.eclipse.collections.api.list.primitive.ByteList bytes) {
        return new String(bytes.toArray(), StandardCharsets.ISO_8859_1);
    }
}
