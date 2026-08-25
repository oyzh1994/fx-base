package cn.oyzh.fx.gui.svg.glyph.file.u;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileUnknownSVGGlyph extends SVGGlyph {

    public FileUnknownSVGGlyph() {
        super("/fx-svg/file/u/file-unknown.svg");
    }

    public FileUnknownSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
