package cn.oyzh.fx.gui.svg.glyph.file.c;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileConfSVGGlyph extends SVGGlyph {

    public FileConfSVGGlyph() {
        super("/fx-svg/file/c/file-conf.svg");
    }

    public FileConfSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
