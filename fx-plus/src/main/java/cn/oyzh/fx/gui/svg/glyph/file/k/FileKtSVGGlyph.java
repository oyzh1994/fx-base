package cn.oyzh.fx.gui.svg.glyph.file.k;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileKtSVGGlyph extends SVGGlyph {

    public FileKtSVGGlyph() {
        super("/fx-svg/file/k/file-kt.svg");
    }

    public FileKtSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
