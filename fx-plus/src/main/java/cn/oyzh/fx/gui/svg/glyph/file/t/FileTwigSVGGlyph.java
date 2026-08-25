package cn.oyzh.fx.gui.svg.glyph.file.t;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileTwigSVGGlyph extends SVGGlyph {

    public FileTwigSVGGlyph() {
        super("/fx-svg/file/t/file-twig.svg");
    }

    public FileTwigSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
