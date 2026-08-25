package cn.oyzh.fx.gui.svg.glyph.file.r;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-06
 */
public class FileRssSVGGlyph extends SVGGlyph {

    public FileRssSVGGlyph() {
        super("/fx-svg/file/r/file-rss.svg");
    }

    public FileRssSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
