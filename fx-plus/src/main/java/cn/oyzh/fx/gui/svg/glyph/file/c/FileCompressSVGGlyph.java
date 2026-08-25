package cn.oyzh.fx.gui.svg.glyph.file.c;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileCompressSVGGlyph extends SVGGlyph {

    public FileCompressSVGGlyph() {
        super("/fx-svg/file/c/file-compress.svg");
    }

    public FileCompressSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
