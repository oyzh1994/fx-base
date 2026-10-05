package com.tangluobo.rdp4j.graphics;

/**
 * The pixel surface the RDP drawing pipeline renders into.
 *
 * <p>Implemented by the JavaFX frontend and by the offscreen scratch buffer.
 * This interface is deliberately free of {@code java.awt} types; pixel data
 * crosses it as plain {@code int[]}.</p>
 *
 * <h2>Palette contract</h2>
 * When a palette is installed the coordinate accessors work in <i>palette
 * index</i> space, because {@link RasterOp} performs raster operations on
 * indices:
 * <ul>
 * <li>{@link #getRGB(int, int)} returns a palette <b>index</b>.</li>
 * <li>{@link #setRGB} takes a palette <b>index</b> and maps it to RGB.</li>
 * <li>{@link #setRGBNoConversion} takes real <b>RGB</b> and stores it as-is.</li>
 * <li>With no palette installed, all three use opaque {@code 0xffRRGGBB}.</li>
 * </ul>
 */
public interface Display {

    /**
     * Force a colour to its true RGB representation (extracting from the palette
     * if indexed colour).
     *
     * @param color color or palette index
     * @return opaque RGB value
     */
    int checkColor(int color);

    /**
     * Creates a platform cursor from a top-down, tightly packed,
     * non-premultiplied {@code 0xAARRGGBB} raster.
     *
     * @param name     cursor name
     * @param hotspotX hotspot x
     * @param hotspotY hotspot y
     * @param pixels   {@code 0xAARRGGBB} pixels, {@code width * height} entries
     * @param width    cursor width
     * @param height   cursor height
     * @return the cursor
     */
    RdpCursor createCursor(String name, int hotspotX, int hotspotY, int[] pixels, int width, int height);

    int getDisplayHeight();

    int getDisplayWidth();

    /** Apply a server-requested pointer position in remote desktop coordinates. */
    default void movePointer(int x, int y) {
    }

    /** Audible bell, for the RDP BELL PDU. */
    default void beep() {
    }

    /**
     * Read one pixel. Returns a palette index when a palette is installed,
     * otherwise opaque {@code 0xffRRGGBB}.
     *
     * @param x x coordinate
     * @param y y coordinate
     * @return palette index or opaque RGB
     */
    int getRGB(int x, int y);

    /**
     * Read a rectangular region.
     *
     * @param x      left edge
     * @param y      top edge
     * @param cx     width
     * @param cy     height
     * @param data   destination, allocated when {@code null}
     * @param offset offset into {@code data}
     * @param width  stride of {@code data}
     * @return the destination array
     */
    int[] getRGB(int x, int y, int cx, int cy, int[] data, int offset, int width);

    void init(RdesktopCanvas canvas);

    void repaint();

    void repaint(int x, int y, int cx, int cy);

    /** Repaint caused by newly decoded remote desktop pixels or drawing orders. */
    default void repaintRemote(int x, int y, int cx, int cy) {
        repaint(x, y, cx, cy);
    }

    /** One-shot notification after the first decoded remote update. */
    default void setFirstRemoteUpdateListener(Runnable listener) {
    }

    /**
     * Resize the surface. Implementations must <b>replace</b> the backing buffer
     * rather than mutate it in place, so that identity comparisons against a
     * previously published buffer keep working.
     *
     * @param width  new width
     * @param height new height
     */
    void resizeDisplay(int width, int height);

    void setCursor(RdpCursor cursor);

    /**
     * Install the colour palette, or clear it with {@code null} for true colour.
     *
     * @param palette palette, or null
     */
    void setPalette(RdpPalette palette);

    /**
     * Write one pixel, mapping it through the palette when one is installed.
     *
     * @param x     x coordinate
     * @param y     y coordinate
     * @param color palette index, or opaque RGB when no palette is installed
     */
    void setRGB(int x, int y, int color);

    /**
     * Write a rectangular region, mapping each value through the palette when
     * one is installed.
     *
     * @param x         left edge
     * @param y         top edge
     * @param cx        width
     * @param cy        height
     * @param data      source pixels (palette indices when indexed)
     * @param offset    offset into {@code data}
     * @param scanWidth stride of {@code data}
     */
    void setRGB(int x, int y, int cx, int cy, int[] data, int offset, int scanWidth);

    /**
     * Apply a given array of colour values to an area of pixels in the image,
     * <b>without</b> palette conversion.
     *
     * @param x         left edge
     * @param y         top edge
     * @param cx        width
     * @param cy        height
     * @param data      source RGB pixels
     * @param offset    offset into {@code data}
     * @param scanWidth stride of {@code data}
     */
    void setRGBNoConversion(int x, int y, int cx, int cy, int[] data, int offset, int scanWidth);
}
