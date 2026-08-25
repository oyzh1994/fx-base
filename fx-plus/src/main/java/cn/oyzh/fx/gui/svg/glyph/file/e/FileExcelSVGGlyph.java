package cn.oyzh.fx.gui.svg.glyph.file.e;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileExcelSVGGlyph extends SVGGlyph {

    public FileExcelSVGGlyph() {
        super("/fx-svg/file/e/file-excel.svg");
    }

    public FileExcelSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
