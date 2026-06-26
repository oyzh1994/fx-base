package cn.oyzh.fx.gui.svg.glyph.file.m;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileMovSVGGlyph extends SVGGlyph {

    public FileMovSVGGlyph() {
        super("/fx-svg/file/m/file-mov.svg");
    }

    public FileMovSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
