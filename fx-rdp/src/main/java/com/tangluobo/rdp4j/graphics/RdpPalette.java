package com.tangluobo.rdp4j.graphics;

/**
 * AWT-free replacement for {@code java.awt.image.IndexColorModel}, providing
 * exactly the two behaviours the RDP drawing pipeline needs:
 *
 * <ul>
 * <li><b>Forward</b> {@link #getRGB(int)} — palette index to opaque
 * {@code 0xffRRGGBB}. Used by {@code Display.setRGB} when an indexed bitmap
 * arrives.</li>
 * <li><b>Reverse</b> {@link #getIndexForRgb(int)} / {@link #getDataElement} —
 * stored RGB back to a palette index. Used by {@code Display.getRGB} so that
 * {@link RasterOp} can perform its raster operations in index space.</li>
 * </ul>
 *
 * <p>That forward/reverse asymmetry is deliberate and load-bearing: with a
 * palette installed, {@code Display.getRGB} returns an <i>index</i> and
 * {@code Display.setRGB} takes an <i>index</i>, while
 * {@code Display.setRGBNoConversion} takes real RGB. Preserve it.</p>
 *
 * <p>Immutable, so it is safe to publish through a volatile field.</p>
 */
public final class RdpPalette {

    /** Maximum number of entries in an RDP colour table. */
    public static final int MAX_SIZE = 256;

    /** Entries indexed by palette index; each is opaque {@code 0xffRRGGBB}. */
    private final int[] rgb;

    /**
     * Open-addressed reverse map over {@code 0x00RRGGBB} keys. A direct
     * {@code int[1 << 24]} table would cost 64 MB, and the reverse lookup runs
     * once per pixel inside {@link RasterOp}, so it must stay allocation-free.
     * {@code -1} marks an empty slot.
     */
    private final int[] keys;
    private final int[] values;
    private final int mask;

    /**
     * Build a palette from the same arguments the old
     * {@code new IndexColorModel(8, size, red, green, blue)} call sites used.
     *
     * @param size  number of valid entries (1..256)
     * @param red   red components, at least {@code size} entries
     * @param green green components, at least {@code size} entries
     * @param blue  blue components, at least {@code size} entries
     */
    public RdpPalette(int size, byte[] red, byte[] green, byte[] blue) {
        if (size < 1 || size > MAX_SIZE) {
            throw new IllegalArgumentException("Palette size out of range: " + size);
        }
        this.rgb = new int[size];
        for (int i = 0; i < size; i++) {
            this.rgb[i] = 0xff000000
                    | ((red[i] & 0xff) << 16)
                    | ((green[i] & 0xff) << 8)
                    | (blue[i] & 0xff);
        }

        int capacity = Integer.highestOneBit(Math.max(2, size * 2 - 1)) << 1;
        this.mask = capacity - 1;
        this.keys = new int[capacity];
        this.values = new int[capacity];
        java.util.Arrays.fill(this.keys, -1);
        for (int i = 0; i < size; i++) {
            int key = this.rgb[i] & 0x00ffffff;
            int slot = spread(key) & mask;
            while (keys[slot] != -1) {
                if (keys[slot] == key) {
                    // Duplicate palette entries: the lowest index wins, matching
                    // IndexColorModel's first-match behaviour.
                    break;
                }
                slot = (slot + 1) & mask;
            }
            if (keys[slot] == -1) {
                keys[slot] = key;
                values[slot] = i;
            }
        }
    }

    /** Mixes the key so that low RGB bits do not collide in the low address bits. */
    private static int spread(int key) {
        int h = key * 0x9E3779B1;
        return h ^ (h >>> 15);
    }

    /**
     * Forward map: palette index to opaque {@code 0xffRRGGBB}, matching
     * {@code IndexColorModel.getRGB(int)}.
     *
     * @param index palette index
     * @return opaque RGB value
     */
    public int getRGB(int index) {
        return rgb[index];
    }

    /**
     * Reverse map from an opaque or RGB-only value. Returns the palette index
     * whose colour matches exactly, or the nearest colour when the buffer holds
     * a value that did not originate from this palette.
     *
     * @param value RGB value, {@code 0x00RRGGBB} or {@code 0xffRRGGBB}
     * @return palette index
     */
    public int getIndexForRgb(int value) {
        int key = value & 0x00ffffff;
        int slot = spread(key) & mask;
        while (keys[slot] != -1) {
            if (keys[slot] == key) {
                return values[slot];
            }
            slot = (slot + 1) & mask;
        }
        return nearestIndex(key);
    }

    /**
     * Reverse map from unnormalised colour components, mirroring
     * {@code IndexColorModel.getDataElement(int[], int)}.
     *
     * @param components component array holding red, green and blue
     * @param offset     index of the red component
     * @return palette index
     */
    public int getDataElement(int[] components, int offset) {
        return getIndexForRgb(((components[offset] & 0xff) << 16)
                | ((components[offset + 1] & 0xff) << 8)
                | (components[offset + 2] & 0xff));
    }

    /**
     * Fallback for values that are not in the palette: smallest squared RGB
     * distance, lowest index winning ties.
     */
    private int nearestIndex(int key) {
        int red = (key >>> 16) & 0xff;
        int green = (key >>> 8) & 0xff;
        int blue = key & 0xff;
        int best = 0;
        int bestDistance = Integer.MAX_VALUE;
        for (int i = 0; i < rgb.length; i++) {
            int candidate = rgb[i];
            int dr = red - ((candidate >>> 16) & 0xff);
            int dg = green - ((candidate >>> 8) & 0xff);
            int db = blue - (candidate & 0xff);
            int distance = dr * dr + dg * dg + db * db;
            if (distance < bestDistance) {
                bestDistance = distance;
                best = i;
            }
        }
        return best;
    }

    /** Number of valid entries. */
    public int size() {
        return rgb.length;
    }
}
