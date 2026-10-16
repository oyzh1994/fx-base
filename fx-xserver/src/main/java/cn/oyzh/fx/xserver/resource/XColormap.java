package cn.oyzh.fx.xserver.resource;

import cn.oyzh.fx.xserver.client.ClientConnection;
import cn.oyzh.fx.xserver.client.ProtocolException;
import cn.oyzh.fx.xserver.display.Screen;
import cn.oyzh.fx.xserver.display.VisualInfo;

/**
 * A colormap maps pixel values to colours for indexed visuals, and doubles as the colour
 * allocator that clients use even on TrueColor visuals ({@code AllocColor} is how toolkits look
 * up RGB values).
 *
 * <p>Storage is a plain ARGB array plus an allocation bitmap. TrueColor colormaps still accept
 * allocations; the returned pixel is the packed value for the visual rather than an index, which
 * is what X does with {@code AllocColor} on a TrueColor screen.
 */
public final class XColormap extends XResource {

    public static final int NONE = 0;
    public static final int ALL = 1;

    private final Screen screen;
    private final VisualInfo visual;
    private final int[] colors;
    private final boolean[] allocated;
    private final boolean predefined;
    /** Marks a run of cells reserved by AllocColorCells, so FreeColors can release them. */
    private final boolean[] reserved;

    public XColormap(int id, ClientConnection owner, Screen screen, VisualInfo visual,
                     boolean predefined) {
        super(id, CLASS_COLORMAP, owner);
        this.screen = screen;
        this.visual = visual;
        this.predefined = predefined;
        int entries = Short.toUnsignedInt(visual.colormapEntries());
        if (entries <= 0) {
            entries = 256;
        }
        this.colors = new int[entries];
        this.allocated = new boolean[entries];
        this.reserved = new boolean[entries];
        if (predefined) {
            // A predictable ramp so clients that read the default colormap see sane colours.
            for (int i = 0; i < entries; i++) {
                colors[i] = rampValue(i, entries, visual);
                allocated[i] = true;
            }
        }
    }

    private static int rampValue(int index, int entries, VisualInfo visual) {
        if (!visual.isTrueColor()) {
            // Grayscale ramp: index 0 is black, the last index is white.
            int level = entries == 1 ? 0 : index * 255 / (entries - 1);
            return 0xFF000000 | (level << 16) | (level << 8) | level;
        }
        // TrueColor: a 3:3:2 style ramp for 256 entries, otherwise a grey ramp.
        if (entries == 256) {
            int r = ((index >> 5) & 0x7) * 255 / 7;
            int g = ((index >> 2) & 0x7) * 255 / 7;
            int b = (index & 0x3) * 255 / 3;
            return 0xFF000000 | (r << 16) | (g << 8) | b;
        }
        int level = entries == 1 ? 0 : index * 255 / (entries - 1);
        return 0xFF000000 | (level << 16) | (level << 8) | level;
    }

    public Screen screen() {
        return screen;
    }

    public VisualInfo visual() {
        return visual;
    }

    public int size() {
        return colors.length;
    }

    public boolean isPredefined() {
        return predefined;
    }

    /** ARGB value for a cell, or null when the cell was never allocated. */
    public int colorAt(int index) {
        if (index < 0 || index >= colors.length) {
            return 0;
        }
        return colors[index];
    }

    public void storeColor(int index, int argb) {
        if (index < 0 || index >= colors.length) {
            throw ProtocolException.badValue(index);
        }
        colors[index] = argb;
        allocated[index] = true;
    }

    /**
     * Allocates a read-only colour cell, reusing an existing cell with the same value on
     * TrueColor visuals so repeated allocations do not exhaust the map.
     */
    public int allocColor(int r, int g, int b) {
        int argb = 0xFF000000 | (r << 16) | (g << 8) | b;
        if (visual.isTrueColor()) {
            return visual.fromArgb(argb);
        }
        for (int i = 0; i < colors.length; i++) {
            if (allocated[i] && colors[i] == argb) {
                return i;
            }
        }
        for (int i = 0; i < colors.length; i++) {
            if (!allocated[i]) {
                colors[i] = argb;
                allocated[i] = true;
                return i;
            }
        }
        throw ProtocolException.badAlloc();
    }

    /** Finds an existing cell with this colour, or -1. Used by {@code LookupColor}. */
    public int findColor(int r, int g, int b) {
        int argb = 0xFF000000 | (r << 16) | (g << 8) | b;
        for (int i = 0; i < colors.length; i++) {
            if (allocated[i] && colors[i] == argb) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Refuses to hand out writable cells on a visual that has none.
     *
     * <p>{@code AllocColorCells} and {@code AllocColorPlanes} ask for <em>writable</em> entries, and
     * only {@code DirectColor}, {@code PseudoColor} and {@code GrayScale} have them: a
     * {@code TrueColor}, {@code StaticColor} or {@code StaticGray} colormap is read-only, and the
     * protocol says both requests answer {@code BadAlloc} there. **Measured with XTS `Xproto`**
     * (2026-10-03): every one of its cases for the two requests failed here and passed on Xvfb with
     * "wanted ERROR - BadAlloc, got REPLY", because this server handed out pixels 0..n-1 as though a
     * TrueColor map were writable. {@code AllocColor} is a different request and still works - a
     * toolkit looking up an RGB value on TrueColor is the normal case.
     */
    private void requireWritableCells() {
        byte visualClass = visual.visualClass();
        boolean writable = visualClass == VisualInfo.DIRECT_COLOR
                || visualClass == VisualInfo.PSEUDO_COLOR
                || visualClass == VisualInfo.GRAY_SCALE;
        if (!writable) {
            throw ProtocolException.badAlloc();
        }
    }

    /**
     * Allocates {@code count} writable colour cells. On a TrueColor visual the "cells" are the
     * packed pixel values themselves, which is what X does as well; on indexed visuals they are
     * colormap indices.
     *
     * @param contiguous when true the caller gets a run of adjacent cells it can address as
     *                   {@code base + i}
     * @param planes     number of planes the client wants to write, or 0 to skip the check
     */
    public int[] allocColorCells(boolean contiguous, int count, int planes) {
        requireWritableCells();
        if (visual.isTrueColor()) {
            if (planes > 0 && (1 << planes) > colors.length) {
                throw ProtocolException.badValue(planes);
            }
            int[] pixels = new int[count];
            for (int i = 0; i < count; i++) {
                pixels[i] = i;
            }
            return pixels;
        }
        if (planes > 0 && (1 << planes) > colors.length) {
            throw ProtocolException.badValue(planes);
        }
        if (count <= 0 || count > colors.length) {
            throw ProtocolException.badValue(count);
        }
        if (!contiguous) {
            int[] scattered = new int[count];
            int found = 0;
            for (int i = 0; i < colors.length && found < count; i++) {
                if (!allocated[i]) {
                    scattered[found++] = i;
                }
            }
            if (found < count) {
                throw ProtocolException.badAlloc();
            }
            for (int pixel : scattered) {
                reserved[pixel] = true;
                allocated[pixel] = true;
            }
            return scattered;
        }
        // Contiguous: slide a window of `count` free cells over the map.
        int run = 0;
        for (int i = 0; i < colors.length; i++) {
            run = allocated[i] ? 0 : run + 1;
            if (run == count) {
                int base = i - count + 1;
                int[] result = new int[count];
                for (int j = 0; j < count; j++) {
                    result[j] = base + j;
                    reserved[base + j] = true;
                    allocated[base + j] = true;
                }
                return result;
            }
        }
        throw ProtocolException.badAlloc();
    }

    /** Allocates colour planes, returning the pixel base and one mask per plane. */
    public PlaneAllocation allocColorPlanes(int count, int planes) {
        requireWritableCells();
        if (planes < 1 || planes > 4) {
            throw ProtocolException.badValue(planes);
        }
        int[] pixels = new int[count];
        int found = 0;
        for (int i = 0; i < colors.length && found < count; i++) {
            if (!allocated[i]) {
                pixels[found++] = i;
            }
        }
        if (found < count) {
            throw ProtocolException.badAlloc();
        }
        for (int pixel : pixels) {
            allocated[pixel] = true;
            reserved[pixel] = true;
        }
        // Give the client the low `planes` bits as its own colour planes.
        int[] planeMasks = new int[planes];
        for (int i = 0; i < planes; i++) {
            planeMasks[i] = 1 << i;
        }
        return new PlaneAllocation(pixels, planeMasks);
    }

    public record PlaneAllocation(int[] pixels, int[] planeMasks) {
    }

    /**
     * Releases colour cells. Cells the client never allocated are ignored, matching the lenient
     * behaviour of real servers.
     */
    public void freeColors(int[] pixels) {
        for (int pixel : pixels) {
            if (pixel >= 0 && pixel < colors.length && !reserved[pixel]) {
                allocated[pixel] = false;
            }
        }
    }

    @Override
    public String typeName() {
        return "colormap";
    }
}
