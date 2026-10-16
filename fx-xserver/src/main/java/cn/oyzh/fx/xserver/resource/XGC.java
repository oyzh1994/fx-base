package cn.oyzh.fx.xserver.resource;

import cn.oyzh.fx.xserver.client.ClientConnection;
import cn.oyzh.fx.xserver.display.Screen;
import cn.oyzh.fx.xserver.display.VisualInfo;
import cn.oyzh.fx.xserver.resource.XFont;
import cn.oyzh.fx.xserver.resource.XResource;

import java.util.ArrayList;
import java.util.List;

/**
 * Graphics context: the drawing state a client sets up once and reuses for many requests.
 *
 * <p>Fields are plain mutable values because the X protocol mutates them in place with
 * {@code ChangeGC}.
 */
public final class XGC extends XResource {

    public static final int GX_CLEAR = 0x0;
    public static final int GX_AND = 0x1;
    public static final int GX_AND_REVERSE = 0x2;
    public static final int GX_COPY = 0x3;
    public static final int GX_AND_INVERTED = 0x4;
    public static final int GX_NOOP = 0x5;
    public static final int GX_XOR = 0x6;
    public static final int GX_OR = 0x7;
    public static final int GX_NOR = 0x8;
    public static final int GX_EQUIV = 0x9;
    public static final int GX_INVERT = 0xA;
    public static final int GX_OR_REVERSE = 0xB;
    public static final int GX_COPY_INVERTED = 0xC;
    public static final int GX_OR_INVERTED = 0xD;
    public static final int GX_NAND = 0xE;
    public static final int GX_SET = 0xF;

    public static final int LINE_SOLID = 0;
    public static final int LINE_ON_OFF_DASH = 1;
    public static final int LINE_DOUBLE_DASH = 2;

    public static final int CAP_NOT_LAST = 0;
    public static final int CAP_BUTT = 1;
    public static final int CAP_ROUND = 2;
    public static final int CAP_PROJECTING = 3;

    public static final int JOIN_MITER = 0;
    public static final int JOIN_ROUND = 1;
    public static final int JOIN_BEVEL = 2;

    public static final int FILL_SOLID = 0;
    public static final int FILL_TILED = 1;
    public static final int FILL_STIPPLED = 2;
    public static final int FILL_OPAQUE_STIPPLED = 3;

    public static final int FILL_EVEN_ODD = 0;
    public static final int FILL_WINDING = 1;

    public static final int SUBWINDOW_CLIP_BY_CHILDREN = 0;
    public static final int SUBWINDOW_INCLUDE_INFERIORS = 1;

    private final Screen screen;

    private int function = GX_COPY;
    private int planeMask = 0xFFFFFFFF;
    /**
     * X11's default foreground is black and its default background is white. Black has to be
     * written as opaque 0xFF000000: a plain 0 is transparent, which a client that relies on the
     * default foreground would draw with and then see nothing - correct in {@code GetImage}, where
     * only the colour is read, but invisible on any display that honours alpha.
     */
    private int foreground = 0xFF000000;
    private int background = 0xFFFFFFFF;
    private int lineWidth = 0;
    private int lineStyle = LINE_SOLID;
    private int capStyle = CAP_BUTT;
    private int joinStyle = JOIN_MITER;
    private int fillStyle = FILL_SOLID;
    private int fillRule = FILL_EVEN_ODD;
    private int arcMode = 0;
    private int tileStippleOriginX;
    private int tileStippleOriginY;
    private int stippleOriginX;
    private int stippleOriginY;
    private int clipOriginX;
    private int clipOriginY;
    private int dashOffset;
    private byte[] dashes = new byte[0];
    private int subwindowMode = SUBWINDOW_CLIP_BY_CHILDREN;
    private boolean graphicsExposures = true;

    private XFont font;
    private XResource tile;
    private XResource stipple;
    private XResource clipMask;

    /** Freely set clip rectangles, used by {@code SetClipRectangles}. */
    private List<Rectangle2> clipRectangles = new ArrayList<>();
    private int clipOrdering = 0;

    private boolean clipByRectangles;

    /**
     * The visual the GC's pixel values are read and written in: the visual of the drawable it was
     * created on.
     *
     * <p>A client that creates a GC on a window with an alpha channel sets its foreground in *that*
     * visual, so a half transparent colour keeps its alpha - and that is the only way a core drawing
     * request can ask for translucency at all (B5). Converting through the screen's default visual
     * instead - which is what this did - forces every colour opaque, and a client's translucent fill
     * arrives as a solid rectangle.
     */
    private VisualInfo visual;

    public XGC(int id, ClientConnection owner, Screen screen) {
        this(id, owner, screen, screen == null ? null : screen.rootVisual());
    }

    public XGC(int id, ClientConnection owner, Screen screen, VisualInfo visual) {
        super(id, CLASS_GC, owner);
        this.screen = screen;
        this.visual = visual != null ? visual : screen == null ? null : screen.rootVisual();
    }

    /** The visual this context's pixel values are converted in. */
    public VisualInfo visual() {
        return visual;
    }

    public Screen screen() {
        return screen;
    }

    public int function() {
        return function;
    }

    public void setFunction(int function) {
        this.function = function;
    }

    public int planeMask() {
        return planeMask;
    }

    public void setPlaneMask(int planeMask) {
        this.planeMask = planeMask;
    }

    public int foreground() {
        return foreground;
    }

    /**
     * Takes a pixel value in the visual the GC will be used with. A GC is not bound to a visual,
     * so the screen's default visual does the conversion; that is exact for the TrueColor visuals
     * every modern client uses.
     */
    public void setForeground(int foreground) {
        this.foreground = convert(foreground);
    }

    public void setForegroundArgb(int argb) {
        this.foreground = argb;
    }

    public int background() {
        return background;
    }

    public void setBackground(int background) {
        this.background = convert(background);
    }

    public void setBackgroundArgb(int argb) {
        this.background = argb;
    }

    private int convert(int pixel) {
        return visual == null ? 0xFF000000 | pixel : visual.toArgb(pixel);
    }

    public int lineWidth() {
        return lineWidth;
    }

    public void setLineWidth(int lineWidth) {
        this.lineWidth = Math.max(0, lineWidth);
    }

    public int lineStyle() {
        return lineStyle;
    }

    public void setLineStyle(int lineStyle) {
        this.lineStyle = lineStyle;
    }

    public int capStyle() {
        return capStyle;
    }

    public void setCapStyle(int capStyle) {
        this.capStyle = capStyle;
    }

    public int joinStyle() {
        return joinStyle;
    }

    public void setJoinStyle(int joinStyle) {
        this.joinStyle = joinStyle;
    }

    public int fillStyle() {
        return fillStyle;
    }

    public void setFillStyle(int fillStyle) {
        this.fillStyle = fillStyle;
    }

    public int fillRule() {
        return fillRule;
    }

    public void setFillRule(int fillRule) {
        this.fillRule = fillRule;
    }

    public int arcMode() {
        return arcMode;
    }

    public void setArcMode(int arcMode) {
        this.arcMode = arcMode;
    }

    public int tileStippleOriginX() {
        return tileStippleOriginX;
    }

    public void setTileStippleOriginX(int value) {
        this.tileStippleOriginX = value;
    }

    public int tileStippleOriginY() {
        return tileStippleOriginY;
    }

    public void setTileStippleOriginY(int value) {
        this.tileStippleOriginY = value;
    }

    public int stippleOriginX() {
        return stippleOriginX;
    }

    public void setStippleOriginX(int value) {
        this.stippleOriginX = value;
    }

    public int stippleOriginY() {
        return stippleOriginY;
    }

    public void setStippleOriginY(int value) {
        this.stippleOriginY = value;
    }

    public int clipOriginX() {
        return clipOriginX;
    }

    public void setClipOriginX(int value) {
        this.clipOriginX = value;
    }

    public int clipOriginY() {
        return clipOriginY;
    }

    public void setClipOriginY(int value) {
        this.clipOriginY = value;
    }

    public int dashOffset() {
        return dashOffset;
    }

    public void setDashOffset(int dashOffset) {
        this.dashOffset = dashOffset;
    }

    public byte[] dashes() {
        return dashes;
    }

    public void setDashes(byte[] dashes) {
        this.dashes = dashes;
    }

    public int subwindowMode() {
        return subwindowMode;
    }

    public void setSubwindowMode(int subwindowMode) {
        this.subwindowMode = subwindowMode;
    }

    public boolean graphicsExposures() {
        return graphicsExposures;
    }

    public void setGraphicsExposures(boolean graphicsExposures) {
        this.graphicsExposures = graphicsExposures;
    }

    public XFont font() {
        return font;
    }

    public void setFont(XFont font) {
        this.font = font;
    }

    public XResource tile() {
        return tile;
    }

    public void setTile(XResource tile) {
        this.tile = tile;
    }

    public XResource stipple() {
        return stipple;
    }

    public void setStipple(XResource stipple) {
        this.stipple = stipple;
    }

    public XResource clipMask() {
        return clipMask;
    }

    public void setClipMask(XResource clipMask) {
        this.clipMask = clipMask;
        this.clipByRectangles = false;
        this.clipRectangles = new ArrayList<>();
    }

    public List<Rectangle2> clipRectangles() {
        return clipRectangles;
    }

    public void setClipRectangles(List<Rectangle2> rectangles, int ordering) {
        this.clipRectangles = rectangles;
        this.clipOrdering = ordering;
        this.clipByRectangles = true;
        this.clipMask = null;
    }

    public boolean isClipByRectangles() {
        return clipByRectangles;
    }

    public int clipOrdering() {
        return clipOrdering;
    }

    /** Rectangle used only by the GC clip list, kept simple to avoid depending on java.awt here. */
    public record Rectangle2(int x, int y, int width, int height) {
    }

    @Override
    public String typeName() {
        return "gcontext";
    }
}
