package cn.oyzh.fx.gui.svg.glyph.file.j;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileJsSVGGlyph extends SVGGlyph {

    public FileJsSVGGlyph() {
        super("/fx-svg/file/j/file-js.svg");
    }

    public FileJsSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
