package cn.oyzh.fx.gui.svg.glyph.file.m;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileMkvSVGGlyph extends SVGGlyph {

    public FileMkvSVGGlyph() {
        super("/fx-svg/file/m/file-mkv.svg");
    }

    public FileMkvSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
