package cn.oyzh.fx.gui.svg.glyph.file.c;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileCppSVGGlyph extends SVGGlyph {

    public FileCppSVGGlyph() {
        super("/fx-svg/file/c/file-cpp.svg");
    }

    public FileCppSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
