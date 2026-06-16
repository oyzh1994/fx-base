package cn.oyzh.fx.gui.svg.glyph.file.c;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileClassSVGGlyph extends SVGGlyph {

    public FileClassSVGGlyph() {
        super("/fx-svg/file/c/file-class.svg");
    }

    public FileClassSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
