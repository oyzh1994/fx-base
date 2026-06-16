package cn.oyzh.fx.gui.svg.glyph.file.w;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileWarSVGGlyph extends SVGGlyph {

    public FileWarSVGGlyph() {
        super("/fx-svg/file/w/file-war.svg");
    }

    public FileWarSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
