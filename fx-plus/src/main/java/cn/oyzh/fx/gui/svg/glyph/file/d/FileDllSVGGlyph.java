package cn.oyzh.fx.gui.svg.glyph.file.d;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-06
 */
public class FileDllSVGGlyph extends SVGGlyph {

    public FileDllSVGGlyph() {
        super("/fx-svg/file/d/file-dll.svg");
    }

    public FileDllSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
