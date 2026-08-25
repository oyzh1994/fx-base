package cn.oyzh.fx.gui.svg.glyph.file.d;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-06
 */
public class FileDylibSVGGlyph extends SVGGlyph {

    public FileDylibSVGGlyph() {
        super("/fx-svg/file/d/file-dylib.svg");
    }

    public FileDylibSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
