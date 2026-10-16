package cn.oyzh.fx.xserver.resource;

import cn.oyzh.fx.xserver.client.ClientConnection;

import java.util.HashMap;
import java.util.Map;

/**
 * A RENDER glyph set: a named collection of glyph images in one format.
 *
 * <p>This is the extension's answer to core X fonts. Instead of a font with a fixed repertoire, a
 * glyph set is filled by the client ({@code AddGlyphs}) with exactly the glyphs it needs, in a
 * format the client picked - coverage only for antialiased text, or full ARGB for colour emoji.
 * {@code CompositeGlyphs8/16/32} then draws strings out of it. Xft uses nothing else.
 *
 * <p>Glyph images are decoded to non-premultiplied {@code 0xAARRGGBB} as they arrive, so the
 * compositor treats a glyph as just another mask picture and never has to decode a depth-1 or
 * depth-8 bitmap in the middle of drawing a string.
 *
 * <p>A glyph set can have more than one name: {@code ReferenceGlyphSet} gives an existing set a
 * second id, and the images live until the last name is freed. That is why the class counts names
 * and why {@link Alias} exists as a separate resource - a resource id can only belong to one
 * {@link XResource}, so the extra names are thin wrappers that share one body.
 */
public final class GlyphSet extends XResource {

    /** One glyph image, in non-premultiplied ARGB, with its metrics. */
    public record Glyph(int width, int height, int x, int y, int xOff, int yOff, int[] argb) {

        /** Alpha coverage at an image pixel as 0..255, or 0 outside the image. */
        public int alphaAt(int px, int py) {
            if (px < 0 || py < 0 || px >= width || py >= height) {
                return 0;
            }
            return (argb[py * width + px] >>> 24) & 0xFF;
        }

        /** Non-premultiplied ARGB at an image pixel, or transparent outside it. */
        public int argbAt(int px, int py) {
            if (px < 0 || py < 0 || px >= width || py >= height) {
                return 0;
            }
            return argb[py * width + px];
        }
    }

    /**
     * Glyph set ids are resolved through the resource table like any other resource, so they need a
     * class of their own rather than {@code CLASS_OTHER}: a stale id would otherwise be reported as
     * a drawable error rather than {@code BadGlyphSet}.
     */
    private static final int RESOURCE_CLASS = XResource.newExtensionClass();

    private final PictFormat format;
    private final Map<Integer, Glyph> glyphs = new HashMap<>();

    /** How many ids name this glyph set; the images go when the count reaches zero. */
    private int names = 1;

    public GlyphSet(int id, ClientConnection owner, PictFormat format) {
        super(id, RESOURCE_CLASS, owner);
        this.format = format;
    }

    public PictFormat format() {
        return format;
    }

    /**
     * True when the format carries colour. The spec calls such a set component-alpha: each channel
     * of a glyph is itself an alpha value rather than a colour, which is what makes subpixel
     * antialiasing and colour glyphs work.
     */
    public boolean isComponentAlpha() {
        return format.isDirect() && format.hasColor();
    }

    /** The glyph with this id, or null when the set does not contain it. */
    public Glyph glyph(int glyphId) {
        return glyphs.get(glyphId);
    }

    public boolean contains(int glyphId) {
        return glyphs.containsKey(glyphId);
    }

    /** Stores a glyph image, replacing any glyph already under the same id. */
    public void put(int glyphId, Glyph glyph) {
        glyphs.put(glyphId, glyph);
    }

    /** Removes a glyph, returning it, or null when it was not there. */
    public Glyph remove(int glyphId) {
        return glyphs.remove(glyphId);
    }

    public int size() {
        return glyphs.size();
    }

    /**
     * Gives this set a second name. The returned resource is what goes in the resource table under
     * the new id; freeing either name decrements the shared count.
     */
    public Alias newAlias(int id, ClientConnection owner) {
        names++;
        return new Alias(id, owner, this);
    }

    /** How many ids currently name this set. */
    public int names() {
        return names;
    }

    private void releaseName() {
        if (--names <= 0) {
            // The last name is gone, so the images are unreachable even though the object lingers
            // until the wrappers are collected. Dropping them now is what bounds memory.
            glyphs.clear();
        }
    }

    @Override
    protected void onDestroy() {
        releaseName();
    }

    @Override
    public String typeName() {
        return "glyphset";
    }

    /**
     * An extra name for a glyph set, as {@code ReferenceGlyphSet} creates. It owns no images; it
     * exists so the resource table can hold the second id and so freeing it is noticed.
     */
    public static final class Alias extends XResource {

        private final GlyphSet target;

        private Alias(int id, ClientConnection owner, GlyphSet target) {
            super(id, RESOURCE_CLASS, owner);
            this.target = target;
        }

        public GlyphSet target() {
            return target;
        }

        @Override
        protected void onDestroy() {
            target.releaseName();
        }

        @Override
        public String typeName() {
            return "glyphset";
        }
    }
}
