package cn.oyzh.fx.gui.svg.glyph.file.p;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FilePdfSVGGlyph extends SVGGlyph {

    public FilePdfSVGGlyph() {
        super("/fx-svg/file/p/file-pdf.svg");
    }

    public FilePdfSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
