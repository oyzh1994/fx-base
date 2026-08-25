package cn.oyzh.fx.gui.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-06
 */
public class FileAddSVGGlyph extends SVGGlyph {

    public FileAddSVGGlyph() {
        super("/fx-svg/file/file-add.svg");
    }

    public FileAddSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
