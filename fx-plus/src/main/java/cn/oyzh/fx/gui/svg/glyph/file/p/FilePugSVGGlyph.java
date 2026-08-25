package cn.oyzh.fx.gui.svg.glyph.file.p;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FilePugSVGGlyph extends SVGGlyph {

    public FilePugSVGGlyph() {
        super("/fx-svg/file/p/file-pug.svg");
    }

    public FilePugSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
