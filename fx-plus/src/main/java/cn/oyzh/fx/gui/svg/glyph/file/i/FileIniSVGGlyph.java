package cn.oyzh.fx.gui.svg.glyph.file.i;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileIniSVGGlyph extends SVGGlyph {

    public FileIniSVGGlyph() {
        super("/fx-svg/file/i/file-ini.svg");
    }

    public FileIniSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
