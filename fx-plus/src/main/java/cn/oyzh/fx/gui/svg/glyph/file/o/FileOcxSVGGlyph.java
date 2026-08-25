package cn.oyzh.fx.gui.svg.glyph.file.o;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileOcxSVGGlyph extends SVGGlyph {

    public FileOcxSVGGlyph() {
        super("/fx-svg/file/o/file-ocx.svg");
    }

    public FileOcxSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
