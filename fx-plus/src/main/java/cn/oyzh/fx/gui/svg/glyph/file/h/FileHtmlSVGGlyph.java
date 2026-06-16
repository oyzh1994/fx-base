package cn.oyzh.fx.gui.svg.glyph.file.h;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileHtmlSVGGlyph extends SVGGlyph {

    public FileHtmlSVGGlyph() {
        super("/fx-svg/file/h/file-html.svg");
    }

    public FileHtmlSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
