package com.tangluobo.rdp4j.graphics;

/**
 * A decoded remote pointer: tightly packed, top-down, <b>non-premultiplied</b>
 * {@code 0xAARRGGBB} pixels. The JavaFX layer premultiplies on its way into an
 * {@code ImageCursor}; keeping the canonical form un-premultiplied here avoids
 * lossy round trips and matches what the RDP pointer PDUs carry.
 */
public final class RdpCursor {

    private final String name;
    private final int hotspotX;
    private final int hotspotY;
    private final int[] pixels;
    private final int width;
    private final int height;

    /**
     * @param name     cursor name, or "hidden" for the system-pointer hidden state
     * @param hotspotX hotspot x in pixels
     * @param hotspotY hotspot y in pixels
     * @param pixels   {@code 0xAARRGGBB} pixels, {@code width * height} entries
     * @param width    cursor width
     * @param height   cursor height
     */
    public RdpCursor(String name, int hotspotX, int hotspotY, int[] pixels, int width, int height) {
        this.name = name;
        this.hotspotX = hotspotX;
        this.hotspotY = hotspotY;
        this.pixels = pixels;
        this.width = width;
        this.height = height;
    }

    public String getName() {
        return name;
    }

    public int getHotspotX() {
        return hotspotX;
    }

    public int getHotspotY() {
        return hotspotY;
    }

    /** The backing array. Callers must treat it as read-only. */
    public int[] getPixels() {
        return pixels;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    /** True when every pixel is fully transparent, i.e. nothing is drawn. */
    public boolean isFullyTransparent() {
        for (int pixel : pixels) {
            if ((pixel >>> 24) != 0) {
                return false;
            }
        }
        return true;
    }
}
