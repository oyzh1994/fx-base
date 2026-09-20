package com.tangluobo.rdp4j.graphics;

/**
 * An offscreen {@link Display} backed by a plain {@code int[]} of opaque
 * {@code 0xffRRGGBB} pixels.
 *
 * <p>This replaces the old Swing {@code WrappedImage}, which was used purely as
 * a scratch target for bitmap decompression before the result was blitted onto
 * the real surface. It implements the same palette contract as the on-screen
 * display, so decompression code can keep writing through
 * {@link #setRGB(int, int, int)} unchanged.</p>
 */
public final class IntArrayDisplay implements Display {

    private final int width;
    private final int height;
    private final int[] pixels;
    private RdpPalette palette;

    public IntArrayDisplay(int width, int height) {
        this.width = width;
        this.height = height;
        this.pixels = new int[width * height];
    }

    /** The backing pixels, opaque {@code 0xffRRGGBB}. Treat as read-only. */
    public int[] getPixels() {
        return pixels;
    }

    @Override
    public int checkColor(int color) {
        RdpPalette current = palette;
        return current == null ? color : current.getRGB(color & 0xff);
    }

    @Override
    public RdpCursor createCursor(String name, int hotspotX, int hotspotY, int[] pixels, int width, int height) {
        return new RdpCursor(name, hotspotX, hotspotY, pixels, width, height);
    }

    @Override
    public int getDisplayHeight() {
        return height;
    }

    @Override
    public int getDisplayWidth() {
        return width;
    }

    @Override
    public int getRGB(int x, int y) {
        int stored = pixels[y * width + x];
        RdpPalette current = palette;
        return current == null ? stored : current.getIndexForRgb(stored & 0x00ffffff);
    }

    @Override
    public int[] getRGB(int x, int y, int cx, int cy, int[] data, int offset, int scanWidth) {
        if (data == null) {
            data = new int[cx * cy];
        }
        for (int row = 0; row < cy; row++) {
            System.arraycopy(pixels, (y + row) * width + x, data, offset + row * scanWidth, cx);
        }
        return data;
    }

    @Override
    public void init(RdesktopCanvas canvas) {
        // Offscreen buffer: nothing to attach to.
    }

    @Override
    public void repaint() {
        // Offscreen buffer: nothing to repaint.
    }

    @Override
    public void repaint(int x, int y, int cx, int cy) {
        // Offscreen buffer: nothing to repaint.
    }

    @Override
    public void resizeDisplay(int width, int height) {
        // Scratch buffers are sized once by their creator.
    }

    @Override
    public void setCursor(RdpCursor cursor) {
        // Offscreen buffer: no pointer.
    }

    @Override
    public void setPalette(RdpPalette palette) {
        this.palette = palette;
    }

    @Override
    public void setRGB(int x, int y, int color) {
        RdpPalette current = palette;
        pixels[y * width + x] = (current == null ? color : current.getRGB(color & 0xff)) | 0xff000000;
    }

    @Override
    public void setRGB(int x, int y, int cx, int cy, int[] data, int offset, int scanWidth) {
        RdpPalette current = palette;
        if (current == null) {
            copyOpaque(x, y, cx, cy, data, offset, scanWidth);
            return;
        }
        for (int row = 0; row < cy; row++) {
            int sourceIndex = offset + row * scanWidth;
            int targetIndex = (y + row) * width + x;
            for (int column = 0; column < cx; column++) {
                pixels[targetIndex + column] = current.getRGB(data[sourceIndex + column] & 0xff) | 0xff000000;
            }
        }
    }

    @Override
    public void setRGBNoConversion(int x, int y, int cx, int cy, int[] data, int offset, int scanWidth) {
        copyOpaque(x, y, cx, cy, data, offset, scanWidth);
    }

    private void copyOpaque(int x, int y, int cx, int cy, int[] data, int offset, int scanWidth) {
        for (int row = 0; row < cy; row++) {
            int sourceIndex = offset + row * scanWidth;
            int targetIndex = (y + row) * width + x;
            for (int column = 0; column < cx; column++) {
                pixels[targetIndex + column] = data[sourceIndex + column] | 0xff000000;
            }
        }
    }
}
