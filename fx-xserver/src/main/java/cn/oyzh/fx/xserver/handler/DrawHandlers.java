package cn.oyzh.fx.xserver.handler;

import com.github.moaxcp.x11.protocol.xproto.ClearArea;
import com.github.moaxcp.x11.protocol.xproto.CopyArea;
import com.github.moaxcp.x11.protocol.xproto.CopyPlane;
import com.github.moaxcp.x11.protocol.xproto.FillPoly;
import com.github.moaxcp.x11.protocol.xproto.GetImage;
import com.github.moaxcp.x11.protocol.xproto.GetImageReply;
import com.github.moaxcp.x11.protocol.xproto.ImageText16;
import com.github.moaxcp.x11.protocol.xproto.ImageText8;
import com.github.moaxcp.x11.protocol.xproto.NoExposureEvent;
import com.github.moaxcp.x11.protocol.xproto.PolyArc;
import com.github.moaxcp.x11.protocol.xproto.PolyFillArc;
import com.github.moaxcp.x11.protocol.xproto.PolyFillRectangle;
import com.github.moaxcp.x11.protocol.xproto.PolyLine;
import com.github.moaxcp.x11.protocol.xproto.PolyPoint;
import com.github.moaxcp.x11.protocol.xproto.PolyRectangle;
import com.github.moaxcp.x11.protocol.xproto.PolySegment;
import com.github.moaxcp.x11.protocol.xproto.PolyText16;
import com.github.moaxcp.x11.protocol.xproto.PolyText8;
import com.github.moaxcp.x11.protocol.xproto.PutImage;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.client.ClientConnection;
import cn.oyzh.fx.xserver.client.ProtocolException;
import cn.oyzh.fx.xserver.display.VisualInfo;
import cn.oyzh.fx.xserver.draw.Framebuffer;
import cn.oyzh.fx.xserver.draw.ImageCodec;
import cn.oyzh.fx.xserver.draw.PixelStore;
import cn.oyzh.fx.xserver.draw.Rasterizer;
import cn.oyzh.fx.xserver.resource.ShapeState;
import cn.oyzh.fx.xserver.resource.XFont;
import cn.oyzh.fx.xserver.resource.XGC;
import cn.oyzh.fx.xserver.resource.XPixmap;
import cn.oyzh.fx.xserver.resource.XResource;
import cn.oyzh.fx.xserver.resource.XWindow;
import org.eclipse.collections.impl.factory.primitive.ByteLists;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/**
 * Drawing requests.
 *
 * <p>Everything funnels through {@link Rasterizer}, which owns the X11 raster operation semantics.
 * This class only has to resolve the drawable, the graphics context and the clip rectangle that the
 * window hierarchy imposes, and translate the request's arguments into calls.
 */
public final class DrawHandlers {

    private static final org.slf4j.Logger LOG =
            org.slf4j.LoggerFactory.getLogger(DrawHandlers.class);


    /** Coordinate modes. */
    private static final int COORD_MODE_ORIGIN = 0;
    private static final int COORD_MODE_PREVIOUS = 1;

    /** Shape values for {@code FillPoly}. */
    private static final int SHAPE_COMPLEX = 0;
    private static final int SHAPE_NONCONVEX = 1;
    private static final int SHAPE_CONVEX = 2;

    private final XServer server;

    public DrawHandlers(XServer server) {
        this.server = server;
    }

    // ------------------------------------------------------------------
    // Area operations
    // ------------------------------------------------------------------

    public void clearArea(ClientConnection client, ClearArea r) {
        XWindow window = window(client, r.getWindow());
        if (!window.isViewable()) {
            return;
        }
        int width = Short.toUnsignedInt(r.getWidth());
        int height = Short.toUnsignedInt(r.getHeight());
        if (width == 0) {
            width = window.width() - r.getX();
        }
        if (height == 0) {
            height = window.height() - r.getY();
        }
        Rectangle area = new Rectangle(window.absoluteX() + r.getX(),
                window.absoluteY() + r.getY(), width, height);
        if (window.keepsOwnPixels()) {
            // A window with pixels of its own clears into its store - the whole area, not only the
            // part of it that shows - and is composited from there (B5). The pixel and the tiled
            // case are the same call: the window painter knows both and the tiling origin.
            server.windows().paintBackground(window, area);
        } else if (window.isBackgroundPixmap()) {
            // A tiled background is painted by the window painter, which knows the tiling origin.
            server.windows().paintBackground(window, area);
        } else {
            // Only the visible pieces of the area belong to this window; the rest is covered by
            // whatever is stacked above it and must be left alone.
            for (Rectangle piece : window.visibleRegion()) {
                Rectangle visible = area.intersection(piece);
                if (!visible.isEmpty()) {
                    window.screen().framebuffer().fillRect(visible.x, visible.y, visible.width,
                            visible.height, window.backgroundPixel());
                }
            }
        }
        if (r.isExposures()) {
            server.windows().deliverExpose(window, area);
        }
        server.markScreenDirty();
    }

    public void copyArea(ClientConnection client, CopyArea r) {
        XResource source = server.resources().requireDrawable(client, r.getSrcDrawable());
        XResource destination = server.resources().requireDrawable(client, r.getDstDrawable());
        XGC gc = gc(client, r.getGc());
        Target target = target(destination, gc);
        if (LOG.isDebugEnabled()) {
            LOG.debug("CopyArea {} -> {} {}x{} from ({},{}) to ({},{})", source, destination,
                    Short.toUnsignedInt(r.getWidth()), Short.toUnsignedInt(r.getHeight()),
                    r.getSrcX(), r.getSrcY(), r.getDstX(), r.getDstY());
        }
        target.rasterizer().copyArea(storeOf(source), originXOf(source) + r.getSrcX(),
                originYOf(source) + r.getSrcY(), target.x(r.getDstX()), target.y(r.getDstY()),
                Short.toUnsignedInt(r.getWidth()), Short.toUnsignedInt(r.getHeight()));
        afterDrawing(client, gc, destination, target, r.getSrcDrawable());
    }

    public void copyPlane(ClientConnection client, CopyPlane r) {
        XResource source = server.resources().requireDrawable(client, r.getSrcDrawable());
        XResource destination = server.resources().requireDrawable(client, r.getDstDrawable());
        XGC gc = gc(client, r.getGc());
        Target target = target(destination, gc);
        if (LOG.isDebugEnabled()) {
            LOG.debug("CopyPlane {} -> {} {}x{} from ({},{}) to ({},{})", source, destination,
                    Short.toUnsignedInt(r.getWidth()), Short.toUnsignedInt(r.getHeight()),
                    r.getSrcX(), r.getSrcY(), r.getDstX(), r.getDstY());
        }
        target.rasterizer().copyPlane(storeOf(source), originXOf(source) + r.getSrcX(),
                originYOf(source) + r.getSrcY(), target.x(r.getDstX()), target.y(r.getDstY()),
                Short.toUnsignedInt(r.getWidth()), Short.toUnsignedInt(r.getHeight()),
                r.getBitPlane());
        afterDrawing(client, gc, destination, target, r.getSrcDrawable());
    }

    public void putImage(ClientConnection client, PutImage r) {
        XResource drawable = server.resources().requireDrawable(client, r.getDrawable());
        XGC gc = gc(client, r.getGc());
        int format = Byte.toUnsignedInt(r.getFormat());
        if (format > ImageCodec.ZPIXMAP) {
            throw ProtocolException.badValue(format);
        }
        // A bitmap carries no depth of its own: its bits are painted in the GC's foreground and
        // background, so it may be put into a drawable of any depth. Only real images have to
        // match, which is how clients draw monochrome icons and cursor shapes into colour windows.
        int depth = Byte.toUnsignedInt(r.getDepth());
        if (format != ImageCodec.XY_BITMAP && depth != 0 && depth != depthOf(drawable)) {
            throw ProtocolException.badMatch();
        }
        Target target = target(drawable, gc);
        if (drawable instanceof XWindow && LOG.isDebugEnabled()) {
            // Drawing into a window is what puts something on the screen; a run in which it never
            // happens says the client is rendering somewhere else and never presenting. Debug and
            // not info: an application uploads every frame this way, so at info this wrote a line
            // per frame to the console - which is where a forwarded JavaFX or GTK client's time
            // went (measured: a synchronous formatted line per PutImage, thousands a second while
            // scrolling).
            LOG.debug("PutImage draws into WINDOW {} at ({},{}) {}x{}",
                    drawable, r.getDstX(), r.getDstY(), Short.toUnsignedInt(r.getWidth()),
                    Short.toUnsignedInt(r.getHeight()));
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("PutImage format={} depth={} data={} bytes {}x{} to {} at ({},{})",
                    format, Byte.toUnsignedInt(r.getDepth()), r.getData().size(),
                    Short.toUnsignedInt(r.getWidth()), Short.toUnsignedInt(r.getHeight()),
                    drawable, r.getDstX(), r.getDstY());
        }
        int width = Short.toUnsignedInt(r.getWidth());
        int height = Short.toUnsignedInt(r.getHeight());
        ImageCodec.putImage(target.store(), target.rasterizer(), target.x(r.getDstX()),
                target.y(r.getDstY()),
                width, height, format, Byte.toUnsignedInt(r.getLeftPad()), depthOf(drawable),
                r.getData().toArray(), 0, client.isBigEndian());
        if (LOG.isDebugEnabled()) {
            // The decoded corners: a put that "succeeds" but lands black is otherwise invisible in
            // the log, and that is exactly the difference between a drawn window and a black one.
            int x = target.x(r.getDstX());
            int y = target.y(r.getDstY());
            LOG.debug("PutImage landed: tl={} tr={} bl={} br={} values={}",
                    hex(target.store().pixel(x, y)),
                    hex(target.store().pixel(x + width - 1, y)),
                    hex(target.store().pixel(x, y + height - 1)),
                    hex(target.store().pixel(x + width - 1, y + height - 1)),
                    values(target.store(), x, y, width, height));
        }
        afterDrawing(client, gc, drawable, target, 0);
    }

    /**
     * The distinct byte values of a small just-written image, low byte first, as a diagnostic: an
     * image whose values are all the same tells a reader nothing, and one whose values are only ever
     * 0x55, 0xaa and 0xff says the coverage arrived pre-quantised instead of smooth.
     */
    private static String values(PixelStore store, int x, int y, int width, int height) {
        if (width * height > 64) {
            return "(too big)";
        }
        java.util.LinkedHashSet<String> seen = new java.util.LinkedHashSet<>();
        for (int row = 0; row < height; row++) {
            for (int column = 0; column < width; column++) {
                seen.add(String.format("%02x", store.pixel(x + column, y + row) & 0xFF));
            }
        }
        return String.join(",", seen);
    }

    public void getImage(ClientConnection client, GetImage r) {
        XResource drawable = server.resources().requireDrawable(client, r.getDrawable());
        // Pixels are the thing an untrusted connection may not read out of somebody else's drawable
        // (F7): a reference refuses this BadAccess and answers everything else about the drawable.
        cn.oyzh.fx.xserver.extension.SecurityExtension.requireOwned(client, drawable,
                "GetImage");
        int format = Byte.toUnsignedInt(r.getFormat());
        if (format > ImageCodec.ZPIXMAP) {
            throw ProtocolException.badValue(format);
        }
        int x = r.getX();
        int y = r.getY();
        int width = Short.toUnsignedInt(r.getWidth());
        int height = Short.toUnsignedInt(r.getHeight());
        // The rectangle is in the drawable's own coordinates and must lie inside it.
        if (x + width > widthOf(drawable) || y + height > heightOf(drawable)
                || width == 0 || height == 0) {
            throw ProtocolException.badMatch();
        }
        VisualInfo visual = visualOf(drawable);
        // Reading needs the surface but no graphics context: GetImage takes its own plane mask.
        PixelStore store = storeOf(drawable);
        byte[] data = ImageCodec.getImage(store, x + originXOf(drawable),
                y + originYOf(drawable),
                width, height, format,
                r.getPlaneMask(), depthOf(drawable), visual, client.isBigEndian());
        client.sendReply(GetImageReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .depth((byte) depthOf(drawable))
                .visual(visual == null ? 0 : visual.id())
                .data(ByteLists.immutable.of(data))
                .build());
    }

    // ------------------------------------------------------------------
    // Primitives
    // ------------------------------------------------------------------

    public void polyPoint(ClientConnection client, PolyPoint r) {
        XGC gc = gc(client, r.getGc());
        XResource drawable = server.resources().requireDrawable(client, r.getDrawable());
        Target target = target(drawable, gc);
        int[] points = target.points(flattenPoints(r.getPoints(), r.getCoordinateMode()));
        target.rasterizer().drawPoints(points);
        afterDrawing(client, gc, drawable, target, 0);
    }

    public void polyLine(ClientConnection client, PolyLine r) {
        XGC gc = gc(client, r.getGc());
        XResource drawable = server.resources().requireDrawable(client, r.getDrawable());
        Target target = target(drawable, gc);
        int[] points = target.points(flattenPoints(r.getPoints(), r.getCoordinateMode()));
        target.rasterizer().drawPolyline(points);
        afterDrawing(client, gc, drawable, target, 0);
    }

    public void polySegment(ClientConnection client, PolySegment r) {
        XGC gc = gc(client, r.getGc());
        XResource drawable = server.resources().requireDrawable(client, r.getDrawable());
        Target target = target(drawable, gc);
        int[] points = new int[r.getSegments().size() * 4];
        int i = 0;
        for (var segment : r.getSegments()) {
            points[i++] = segment.getX1();
            points[i++] = segment.getY1();
            points[i++] = segment.getX2();
            points[i++] = segment.getY2();
        }
        target.rasterizer().drawSegments(target.points(points));
        afterDrawing(client, gc, drawable, target, 0);
    }

    public void polyRectangle(ClientConnection client, PolyRectangle r) {
        XGC gc = gc(client, r.getGc());
        XResource drawable = server.resources().requireDrawable(client, r.getDrawable());
        Target target = target(drawable, gc);
        for (var rectangle : r.getRectangles()) {
            target.rasterizer().drawRect(target.x(rectangle.getX()), target.y(rectangle.getY()),
                    Short.toUnsignedInt(rectangle.getWidth()),
                    Short.toUnsignedInt(rectangle.getHeight()));
        }
        afterDrawing(client, gc, drawable, target, 0);
    }

    public void polyArc(ClientConnection client, PolyArc r) {
        XGC gc = gc(client, r.getGc());
        XResource drawable = server.resources().requireDrawable(client, r.getDrawable());
        Target target = target(drawable, gc);
        for (var arc : r.getArcs()) {
            target.rasterizer().drawArc(target.x(arc.getX()), target.y(arc.getY()),
                    Short.toUnsignedInt(arc.getWidth()),
                    Short.toUnsignedInt(arc.getHeight()), arc.getAngle1(), arc.getAngle2());
        }
        afterDrawing(client, gc, drawable, target, 0);
    }

    public void polyFillRectangle(ClientConnection client, PolyFillRectangle r) {
        XGC gc = gc(client, r.getGc());
        XResource drawable = server.resources().requireDrawable(client, r.getDrawable());
        Target target = target(drawable, gc);
        for (var rectangle : r.getRectangles()) {
            target.rasterizer().fillRect(target.x(rectangle.getX()), target.y(rectangle.getY()),
                    Short.toUnsignedInt(rectangle.getWidth()),
                    Short.toUnsignedInt(rectangle.getHeight()));
        }
        afterDrawing(client, gc, drawable, target, 0);
    }

    public void polyFillArc(ClientConnection client, PolyFillArc r) {
        XGC gc = gc(client, r.getGc());
        XResource drawable = server.resources().requireDrawable(client, r.getDrawable());
        Target target = target(drawable, gc);
        for (var arc : r.getArcs()) {
            target.rasterizer().fillArc(target.x(arc.getX()), target.y(arc.getY()),
                    Short.toUnsignedInt(arc.getWidth()),
                    Short.toUnsignedInt(arc.getHeight()), arc.getAngle1(), arc.getAngle2());
        }
        afterDrawing(client, gc, drawable, target, 0);
    }

    public void fillPoly(ClientConnection client, FillPoly r) {
        XGC gc = gc(client, r.getGc());
        XResource drawable = server.resources().requireDrawable(client, r.getDrawable());
        Target target = target(drawable, gc);
        int[] points = target.points(flattenPoints(r.getPoints(), r.getCoordinateMode()));
        boolean winding = gc.fillRule() == XGC.FILL_WINDING;
        target.rasterizer().fillPolygon(points, winding);
        afterDrawing(client, gc, drawable, target, 0);
    }

    // ------------------------------------------------------------------
    // Text
    // ------------------------------------------------------------------

    public void polyText8(ClientConnection client, PolyText8 r) {
        XGC gc = gc(client, r.getGc());
        XResource drawable = server.resources().requireDrawable(client, r.getDrawable());
        Target target = target(drawable, gc);
        byte[] items = r.getItems().toArray();
        int x = r.getX();
        int y = r.getY();
        XFont font = gc.font() != null ? gc.font() : defaultFont(client);
        int index = 0;
        while (index < items.length) {
            int length = items[index] & 0xFF;
            index++;
            if (length == 0) {
                break;
            }
            if (length == 255) {
                // Font change item: the next four bytes name the font to use from here on.
                if (index + 4 > items.length) {
                    throw ProtocolException.badLength();
                }
                int fontId = readCard32(items, index, client.isBigEndian());
                index += 4;
                font = (XFont) server.resources().require(client, fontId, XResource.CLASS_FONT,
                        false);
                continue;
            }
            // A text element is [length][delta][characters], in that order: the delta is read and
            // applied before the string it belongs to. Reading the characters straight after the
            // length - the obvious mistake - shifts every string by one byte, which shows up as a
            // missing first character and a dropped last one.
            if (index + 1 + length > items.length) {
                throw ProtocolException.badLength();
            }
            x += items[index];
            index++;
            target.rasterizer().drawText8(font, target.x(x), target.y(y), items, index, length);
            x += font.stringWidth8(items, index, length);
            index += length;
        }
        afterDrawing(client, gc, drawable, target, 0);
    }

    public void polyText16(ClientConnection client, PolyText16 r) {
        XGC gc = gc(client, r.getGc());
        XResource drawable = server.resources().requireDrawable(client, r.getDrawable());
        Target target = target(drawable, gc);
        byte[] items = r.getItems().toArray();
        int x = r.getX();
        int y = r.getY();
        XFont font = gc.font() != null ? gc.font() : defaultFont(client);
        int index = 0;
        while (index < items.length) {
            int length = items[index] & 0xFF;
            index++;
            if (length == 0) {
                break;
            }
            if (length == 255) {
                if (index + 4 > items.length) {
                    throw ProtocolException.badLength();
                }
                int fontId = readCard32(items, index, client.isBigEndian());
                index += 4;
                font = (XFont) server.resources().require(client, fontId, XResource.CLASS_FONT,
                        false);
                continue;
            }
            // Each character is two bytes and the length field counts characters; the delta comes
            // before them, exactly as in PolyText8.
            int bytes = length * 2;
            if (index + 1 + bytes > items.length) {
                throw ProtocolException.badLength();
            }
            x += items[index];
            index++;
            char[] chars = new char[length];
            for (int i = 0; i < length; i++) {
                int high = items[index + i * 2] & 0xFF;
                int low = items[index + i * 2 + 1] & 0xFF;
                chars[i] = (char) ((high << 8) | low);
            }
            target.rasterizer().drawText16(font, target.x(x), target.y(y), chars, 0, length);
            x += font.stringWidth16(chars, 0, length);
            index += bytes;
        }
        afterDrawing(client, gc, drawable, target, 0);
    }

    public void imageText8(ClientConnection client, ImageText8 r) {
        XGC gc = gc(client, r.getGc());
        XResource drawable = server.resources().requireDrawable(client, r.getDrawable());
        Target target = target(drawable, gc);
        byte[] chars = r.getString().toArray();
        XFont font = gc.font() != null ? gc.font() : defaultFont(client);
        target.rasterizer().drawText8(font, target.x(r.getX()), target.y(r.getY()), chars, 0,
                chars.length);
        afterDrawing(client, gc, drawable, target, 0);
    }

    public void imageText16(ClientConnection client, ImageText16 r) {
        XGC gc = gc(client, r.getGc());
        XResource drawable = server.resources().requireDrawable(client, r.getDrawable());
        Target target = target(drawable, gc);
        org.eclipse.collections.api.list.ImmutableList<com.github.moaxcp.x11.protocol.xproto.Char2b> source = r.getString();
        char[] chars = new char[source.size()];
        for (int i = 0; i < chars.length; i++) {
            chars[i] = (char) (((source.get(i).getByte1() & 0xFF) << 8)
                    | (source.get(i).getByte2() & 0xFF));
        }
        XFont font = gc.font() != null ? gc.font() : defaultFont(client);
        target.rasterizer().drawText16(font, target.x(r.getX()), target.y(r.getY()), chars, 0,
                chars.length);
        afterDrawing(client, gc, drawable, target, 0);
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private XWindow window(ClientConnection client, int id) {
        return (XWindow) server.resources().require(client, id, XResource.CLASS_WINDOW, false);
    }

    private static String hex(int argb) {
        return "0x" + Integer.toHexString(argb);
    }

    private XGC gc(ClientConnection client, int id) {
        return (XGC) server.resources().require(client, id, XResource.CLASS_GC, false);
    }

    private XFont defaultFont(ClientConnection client) {
        XResource font = server.resources().get(server.defaultFontId());
        if (font instanceof XFont xFont) {
            return xFont;
        }
        throw ProtocolException.badFont(0);
    }

    private PixelStore storeOf(XResource drawable) {
        if (drawable instanceof XWindow window) {
            // A window with pixels of its own is read from them: what the client drew is what it
            // gets back, whether or not the window is showing (B5).
            return window.keepsOwnPixels() ? window.ownPixels() : window.screen().framebuffer();
        }
        return (XPixmap) drawable;
    }

    private VisualInfo visualOf(XResource drawable) {
        if (drawable instanceof XWindow window) {
            return window.visual();
        }
        return ((XPixmap) drawable).visual();
    }

    private static int widthOf(XResource drawable) {
        return drawable instanceof XWindow window ? window.width()
                : ((XPixmap) drawable).width();
    }

    private static int heightOf(XResource drawable) {
        return drawable instanceof XWindow window ? window.height()
                : ((XPixmap) drawable).height();
    }

    private int depthOf(XResource drawable) {
        if (drawable instanceof XWindow window) {
            return window.depth();
        }
        return ((XPixmap) drawable).depth();
    }

    /**
     * Builds a rasterizer for a drawable. Windows draw into the screen framebuffer clipped to the
     * part of themselves that is actually visible, which is what makes obscuring behave the way X
     * clients expect.
     */
    /**
     * The surface a request draws on, with the translation between the request's coordinates and
     * the surface's own.
     *
     * <p>A window's coordinates are relative to its parent, but a window draws straight into the
     * screen framebuffer, so every coordinate has to be moved by the window's absolute position.
     * Pixmaps are their own origin. Without this a window's contents land wherever its coordinates
     * point on the screen - which is only correct for the root window, and is why the bug survived
     * until a real application was run.
     */
    private record Target(PixelStore store, Rasterizer rasterizer, int originX, int originY) {
        int x(int local) {
            return local + originX;
        }

        int y(int local) {
            return local + originY;
        }

        /** Translates a flat x,y,x,y... point array in place. */
        int[] points(int[] local) {
            for (int i = 0; i < local.length; i += 2) {
                local[i] += originX;
                local[i + 1] += originY;
            }
            return local;
        }
    }

    /**
     * The parts of a window a request may draw into: the window's visible region - its own
     * rectangle minus whatever is stacked above it - narrowed by the GC's own clip rectangles.
     *
     * <p>The rasterizer enforces the GC clip too, but only through its bounding box, so the exact
     * intersection of the two rectangle lists is taken here, where both are available. A clip mask
     * is left to the rasterizer, which tests it per pixel.
     */
    /**
     * The parts of a store window a request may draw into: all of it, narrowed by the GC's clip.
     *
     * <p>Not {@link #clipRegion}, which subtracts what is stacked above the window: a window whose
     * pixels are kept draws whether or not it shows, because the store is what will be composited
     * when it is uncovered again.
     */
    private static List<Rectangle> ownPixelRegion(XWindow window, XGC gc) {
        List<Rectangle> region = window.ownRegion();
        if (gc.isClipByRectangles()) {
            List<Rectangle> clip = new ArrayList<>();
            for (XGC.Rectangle2 r : gc.clipRectangles()) {
                clip.add(new Rectangle(r.x() + gc.clipOriginX(), r.y() + gc.clipOriginY(),
                        r.width(), r.height()));
            }
            region = ShapeState.intersect(region, clip);
        }
        return region;
    }

    private List<Rectangle> clipRegion(XWindow window, XGC gc) {
        List<Rectangle> region = window.visibleRegion();
        if (gc.isClipByRectangles()) {
            List<XGC.Rectangle2> list = gc.clipRectangles();
            List<Rectangle> clip = new ArrayList<>(list.size());
            for (XGC.Rectangle2 r : list) {
                // Rectangle coordinates are relative to the clip origin.
                clip.add(new Rectangle(r.x() + gc.clipOriginX(), r.y() + gc.clipOriginY(),
                        r.width(), r.height()));
            }
            region = ShapeState.intersect(region, clip);
        }
        return region;
    }

    private Target target(XResource drawable, XGC gc) {
        if (drawable instanceof XWindow window) {
            if (window.keepsOwnPixels()) {
                // **A window with pixels of its own draws into them** (B5): the request is applied in
                // the window's own coordinates, to the whole of the window rather than to the part of
                // it that shows - a covered window goes on drawing, which is the point of keeping
                // them - and what is on screen is composited from the store afterwards
                // (XServer.compositeOwnPixels). The clip is therefore the window's rectangle and the
                // GC's own, both of which are already window-relative.
                Framebuffer pixels = window.ownPixels();
                List<Rectangle> region = ownPixelRegion(window, gc);
                return new Target(pixels, new Rasterizer(pixels, gc, ShapeState.extents(region),
                        region), 0, 0);
            }
            PixelStore store = window.screen().framebuffer();
            List<Rectangle> region = clipRegion(window, gc);
            return new Target(store, new Rasterizer(store, gc, ShapeState.extents(region), region),
                    window.absoluteX(), window.absoluteY());
        }
        XPixmap pixmap = (XPixmap) drawable;
        return new Target(pixmap, new Rasterizer(pixmap, gc), 0, 0);
    }

    /** Origin of a drawable in screen coordinates, for requests that name two drawables. */
    /**
     * A drawable's origin: where its own (0,0) is on the screen, or on nothing at all.
     *
     * <p>A window with pixels of its own has its origin at 0, because those pixels *are* its
     * coordinates - there is no other surface underneath them to be offset from (B5).
     */
    private static int originXOf(XResource drawable) {
        if (drawable instanceof XWindow window && window.keepsOwnPixels()) {
            return 0;
        }
        return drawable instanceof XWindow window ? window.absoluteX() : 0;
    }

    private static int originYOf(XResource drawable) {
        if (drawable instanceof XWindow window && window.keepsOwnPixels()) {
            return 0;
        }
        return drawable instanceof XWindow window ? window.absoluteY() : 0;
    }

    /**
     * Finishes a drawing request: marks the screen dirty and reports graphics exposures when the
     * client asked for them.
     *
     * <p>The server always has the source pixels available, so a copy either succeeds completely or
     * is clipped away; a {@code NoExpose} tells the client there is nothing else coming, which is
     * what clients that set {@code graphics-exposures} on their GC are waiting to hear.
     */
    private void afterDrawing(ClientConnection client, XGC gc, XResource drawable, Target target,
                              int sourceDrawable) {
        // Only the screen the drawable belongs to can have changed. A pixmap is not on screen at
        // all, so it is marked on its own screen for the same reason the single screen used to be
        // marked for it: nothing that reads this knows better yet.
        //
        // *What* changed is the region this request wrote, which the rasterizer recorded as it
        // wrote it. Marking the whole screen instead - which is what this did until D2 needed
        // otherwise - is correct and useless: a keystroke in a forwarded terminal repainted the
        // screen, and the backend that shows it over the network sent four megabytes for it.
        Rectangle drawn = target.rasterizer().drawnBounds();
        if (drawn != null) {
            if (drawable instanceof XWindow window) {
                if (window.keepsOwnPixels()) {
                    // What was drawn is in the window's own pixels, in its own coordinates; what the
                    // screen shows is composited from them.
                    server.compositeOwnPixels(window, drawn);
                    drawn = null;                       // the screen's damage is the composite's
                }
            }
            if (drawn != null && drawable instanceof XWindow window) {
                server.markScreenDirty(window.screen(), drawn);
            } else if (drawn != null) {
                server.markScreenDirty(((XPixmap) drawable).screen(), drawn);
            }
            // And a client that asked to be told what changed is told here, with the same region and
            // in the same breath - which is the whole of this extension's hook into drawing.
            cn.oyzh.fx.xserver.extension.DamageExtension.drew(server, drawable, drawn);
        }
        // Only the copy requests have anything to say: `GraphicsExpose`/`NoExpose` answer "what
        // happened to the part I could not copy", and a fill copies nothing. Sending one for every
        // drawing request - which is what this did - is a stray event into a client that is very
        // likely waiting for one from an earlier `CopyArea`, and a reference server sends none.
        // Measured 2026-09-26 with `tools/DamageProbe`, whose event reader printed "other event type
        // 14" for every fill here and nothing at all there.
        if (gc.graphicsExposures() && sourceDrawable != 0 && drawable instanceof XWindow window) {
            server.events().send(window.owner() == null ? client : window.owner(),
                    NoExposureEvent.builder()
                            .firstEventOffset((byte) 0)
                            .sentEvent(false)
                            .sequenceNumber(client.sequenceNumber())
                            .drawable(window.id())
                            .minorOpcode((short) 0)
                            .majorOpcode((byte) 0)
                            .build());
        }
    }

    /** Flattens the point list, resolving relative coordinates when the mode asks for it. */
    private int[] flattenPoints(org.eclipse.collections.api.list.ImmutableList<com.github.moaxcp.x11.protocol.xproto.Point> points, int mode) {
        int[] result = new int[points.size() * 2];
        int lastX = 0;
        int lastY = 0;
        for (int i = 0; i < points.size(); i++) {
            var point = points.get(i);
            if (mode == COORD_MODE_PREVIOUS && i > 0) {
                lastX += point.getX();
                lastY += point.getY();
            } else {
                lastX = point.getX();
                lastY = point.getY();
            }
            result[i * 2] = lastX;
            result[i * 2 + 1] = lastY;
        }
        return result;
    }

    private static int readCard32(byte[] bytes, int offset, boolean bigEndian) {
        if (bigEndian) {
            return ((bytes[offset] & 0xFF) << 24) | ((bytes[offset + 1] & 0xFF) << 16)
                    | ((bytes[offset + 2] & 0xFF) << 8) | (bytes[offset + 3] & 0xFF);
        }
        return ((bytes[offset + 3] & 0xFF) << 24) | ((bytes[offset + 2] & 0xFF) << 16)
                | ((bytes[offset + 1] & 0xFF) << 8) | (bytes[offset] & 0xFF);
    }
}
