package cn.oyzh.fx.gui.svg.glyph.file.w;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileWebpSVGGlyph extends SVGGlyph {

    public FileWebpSVGGlyph() {
        super("/fx-svg/file/w/file-webp.svg");
    }

    public FileWebpSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
