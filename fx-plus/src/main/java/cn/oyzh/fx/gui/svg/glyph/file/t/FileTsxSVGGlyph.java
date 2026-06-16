package cn.oyzh.fx.gui.svg.glyph.file.t;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileTsxSVGGlyph extends SVGGlyph {

    public FileTsxSVGGlyph() {
        super("/fx-svg/file/t/file-tsx.svg");
    }

    public FileTsxSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
