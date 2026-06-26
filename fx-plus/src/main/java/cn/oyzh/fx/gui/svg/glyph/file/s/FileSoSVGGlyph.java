package cn.oyzh.fx.gui.svg.glyph.file.s;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-06
 */
public class FileSoSVGGlyph extends SVGGlyph {

    public FileSoSVGGlyph() {
        super("/fx-svg/file/s/file-so.svg");
    }

    public FileSoSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
