package cn.oyzh.fx.gui.svg.glyph.file.i;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileIcoSVGGlyph extends SVGGlyph {

    public FileIcoSVGGlyph() {
        super("/fx-svg/file/i/file-ico.svg");
    }

    public FileIcoSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
