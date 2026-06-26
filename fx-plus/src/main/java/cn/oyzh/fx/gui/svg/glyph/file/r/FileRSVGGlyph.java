package cn.oyzh.fx.gui.svg.glyph.file.r;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileRSVGGlyph extends SVGGlyph {

    public FileRSVGGlyph() {
        super("/fx-svg/file/r/file-r.svg");
    }

    public FileRSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
