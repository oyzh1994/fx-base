package cn.oyzh.fx.gui.svg.glyph.file.c;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileCudaSVGGlyph extends SVGGlyph {

    public FileCudaSVGGlyph() {
        super("/fx-svg/file/c/file-cuda.svg");
    }

    public FileCudaSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
