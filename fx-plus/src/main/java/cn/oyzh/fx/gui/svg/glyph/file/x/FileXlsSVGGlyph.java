package cn.oyzh.fx.gui.svg.glyph.file.x;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileXlsSVGGlyph extends SVGGlyph {

    public FileXlsSVGGlyph() {
        super("/fx-svg/file/x/file-xls.svg");
    }

    public FileXlsSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
