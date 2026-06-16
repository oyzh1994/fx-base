package cn.oyzh.fx.gui.svg.glyph.file.x;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileXmlSVGGlyph extends SVGGlyph {

    public FileXmlSVGGlyph() {
        super("/fx-svg/file/x/file-xml.svg");
    }

    public FileXmlSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
