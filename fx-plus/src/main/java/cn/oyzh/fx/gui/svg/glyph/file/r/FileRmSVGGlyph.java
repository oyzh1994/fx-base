package cn.oyzh.fx.gui.svg.glyph.file.r;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileRmSVGGlyph extends SVGGlyph {

    public FileRmSVGGlyph() {
        super("/fx-svg/file/r/file-rm.svg");
    }

    public FileRmSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
