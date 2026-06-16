package cn.oyzh.fx.gui.svg.glyph.file.d;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileDiffSVGGlyph extends SVGGlyph {

    public FileDiffSVGGlyph() {
        super("/fx-svg/file/d/file-diff.svg");
    }

    public FileDiffSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
