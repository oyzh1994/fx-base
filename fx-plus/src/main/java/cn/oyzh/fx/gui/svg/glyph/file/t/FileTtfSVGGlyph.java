package cn.oyzh.fx.gui.svg.glyph.file.t;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileTtfSVGGlyph extends SVGGlyph {

    public FileTtfSVGGlyph() {
        super("/fx-svg/file/t/file-ttf.svg");
    }

    public FileTtfSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
