package cn.oyzh.fx.gui.svg.glyph.file.j;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-06
 */
public class FileJpgSVGGlyph extends SVGGlyph {

    public FileJpgSVGGlyph() {
        super("/fx-svg/file/j/file-jpg.svg");
    }

    public FileJpgSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
