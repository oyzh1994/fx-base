package cn.oyzh.fx.gui.svg.glyph.file.r;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileRazorSVGGlyph extends SVGGlyph {

    public FileRazorSVGGlyph() {
        super("/fx-svg/file/r/file-razor.svg");
    }

    public FileRazorSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
