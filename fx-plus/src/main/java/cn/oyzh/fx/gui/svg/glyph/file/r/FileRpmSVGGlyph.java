package cn.oyzh.fx.gui.svg.glyph.file.r;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileRpmSVGGlyph extends SVGGlyph {

    public FileRpmSVGGlyph() {
        super("/fx-svg/file/r/file-rpm.svg");
    }

    public FileRpmSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
