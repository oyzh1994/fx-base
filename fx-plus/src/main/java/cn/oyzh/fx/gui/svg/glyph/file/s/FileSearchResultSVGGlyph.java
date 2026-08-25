package cn.oyzh.fx.gui.svg.glyph.file.s;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileSearchResultSVGGlyph extends SVGGlyph {

    public FileSearchResultSVGGlyph() {
        super("/fx-svg/file/s/file-search-result.svg");
    }

    public FileSearchResultSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
