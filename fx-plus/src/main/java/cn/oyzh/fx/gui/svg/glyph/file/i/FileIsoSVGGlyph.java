package cn.oyzh.fx.gui.svg.glyph.file.i;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-06
 */
public class FileIsoSVGGlyph extends SVGGlyph {

    public FileIsoSVGGlyph() {
        super("/fx-svg/file/i/file-iso.svg");
    }

    public FileIsoSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
