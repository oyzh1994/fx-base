package cn.oyzh.fx.gui.svg.glyph.file.s;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileShSVGGlyph extends SVGGlyph {

    public FileShSVGGlyph() {
        super("/fx-svg/file/s/file-sh.svg");
    }

    public FileShSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
