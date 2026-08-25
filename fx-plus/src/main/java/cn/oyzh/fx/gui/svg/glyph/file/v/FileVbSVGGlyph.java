package cn.oyzh.fx.gui.svg.glyph.file.v;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileVbSVGGlyph extends SVGGlyph {

    public FileVbSVGGlyph() {
        super("/fx-svg/file/v/file-vb.svg");
    }

    public FileVbSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
