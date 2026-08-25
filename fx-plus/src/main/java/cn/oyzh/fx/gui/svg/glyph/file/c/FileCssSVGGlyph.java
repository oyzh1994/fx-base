package cn.oyzh.fx.gui.svg.glyph.file.c;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileCssSVGGlyph extends SVGGlyph {

    public FileCssSVGGlyph() {
        super("/fx-svg/file/c/file-css.svg");
    }

    public FileCssSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
