package cn.oyzh.fx.gui.svg.glyph.file.t;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileTsSVGGlyph extends SVGGlyph {

    public FileTsSVGGlyph() {
        super("/fx-svg/file/t/file-ts.svg");
    }

    public FileTsSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
