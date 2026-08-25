package cn.oyzh.fx.gui.svg.glyph.file.s;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileSqlSVGGlyph extends SVGGlyph {

    public FileSqlSVGGlyph() {
        super("/fx-svg/file/s/file-sql.svg");
    }

    public FileSqlSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
