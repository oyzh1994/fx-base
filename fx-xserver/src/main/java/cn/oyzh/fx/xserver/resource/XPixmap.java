package cn.oyzh.fx.xserver.resource;

import cn.oyzh.fx.xserver.client.ClientConnection;
import cn.oyzh.fx.xserver.display.Screen;
import cn.oyzh.fx.xserver.display.VisualInfo;
import cn.oyzh.fx.xserver.draw.PixelStore;

/**
 * An off screen drawable. Pixmaps own their pixels; they have no position on the screen and
 * receive no events.
 */
public final class XPixmap extends XResource implements PixelStore {

    private final Screen screen;
    private final int width;
    private final int height;
    private final int depth;
    private final VisualInfo visual;
    private final int[] pixels;

    /**
     * Set only on a pixmap that is a <em>view</em> of another surface rather than one that owns its
     * pixels: every access resolves the surface again, so the view follows it.
     *
     * <p>It has to be resolved per access and not held, because both surfaces this is used with
     * move their pixels when they change size - a window's store is replaced on a resize
     * ({@code XWindow.ownPixels}) and the screen's array is reallocated when a client grows it. A
     * view that cached the array would go on reading the old one, which is a stale picture and looks
     * exactly like a client that stopped drawing.
     */
    private final java.util.function.Supplier<PixelStore> view;

    public XPixmap(int id, ClientConnection owner, Screen screen, int width, int height, int depth,
                   VisualInfo visual) {
        this(id, owner, screen, width, height, depth, visual, null);
    }

    private XPixmap(int id, ClientConnection owner, Screen screen, int width, int height, int depth,
                    VisualInfo visual, java.util.function.Supplier<PixelStore> view) {
        super(id, CLASS_PIXMAP, owner);
        this.screen = screen;
        this.width = Math.max(1, width);
        this.height = Math.max(1, height);
        this.depth = depth;
        this.visual = visual;
        this.pixels = view == null ? new int[this.width * this.height] : null;
        this.view = view;
    }

    /**
     * A pixmap that <em>is</em> another surface rather than a copy of it: reading it reads that
     * surface and drawing into it draws there, which is what {@code Composite}'s
     * {@code NameWindowPixmap} hands a compositor - the window's own pixels, live.
     *
     * <p>The size follows the surface, where the protocol says a named pixmap becomes invalid when
     * its window is resized and the compositor must name a new one. Following it is a superset of
     * that: a compositor that does the right thing is unaffected, and one that reads the old pixmap
     * gets the window's pixels instead of a stale picture.
     *
     * <p>Writing into it is allowed, and that is measured rather than assumed:
     * {@code tools/CompositeProbe} fills a named window pixmap on a reference and the fill lands
     * (the specification calls the contents the window's and leaves this undefined, so what a
     * reference *does* is the only honest answer).
     */
    public static XPixmap view(int id, ClientConnection owner, Screen screen, int depth,
                               VisualInfo visual, java.util.function.Supplier<PixelStore> surface) {
        PixelStore first = surface.get();
        return new XPixmap(id, owner, screen, first.width(), first.height(), depth, visual, surface);
    }

    /** The surface this pixmap reads and writes, or null when it owns its pixels. */
    private PixelStore surface() {
        return view == null ? null : view.get();
    }

    public Screen screen() {
        return screen;
    }

    public int depth() {
        return depth;
    }

    public VisualInfo visual() {
        return visual;
    }

    @Override
    public int width() {
        PixelStore surface = surface();
        return surface == null ? width : surface.width();
    }

    @Override
    public int height() {
        PixelStore surface = surface();
        return surface == null ? height : surface.height();
    }

    @Override
    public int stride() {
        PixelStore surface = surface();
        return surface == null ? width : surface.stride();
    }

    @Override
    public int[] pixels() {
        PixelStore surface = surface();
        return surface == null ? pixels : surface.pixels();
    }

    @Override
    public int pixel(int x, int y) {
        PixelStore surface = surface();
        if (surface == null) {
            if (!contains(x, y)) {
                return 0;
            }
            return pixels[y * width + x];
        }
        return surface.pixel(x, y);
    }

    @Override
    public void setPixel(int x, int y, int argb) {
        PixelStore surface = surface();
        if (surface == null) {
            if (!contains(x, y)) {
                return;
            }
            pixels[y * width + x] = argb;
            return;
        }
        surface.setPixel(x, y, argb);
    }

    @Override
    public String typeName() {
        return "pixmap";
    }
}
