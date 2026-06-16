package cn.oyzh.fx.gui.svg.glyph.file.x;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileXslSVGGlyph extends SVGGlyph {

    public FileXslSVGGlyph() {
        super("/fx-svg/file/x/file-xsl.svg");
    }

    public FileXslSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
