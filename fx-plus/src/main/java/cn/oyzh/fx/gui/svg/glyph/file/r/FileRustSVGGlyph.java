package cn.oyzh.fx.gui.svg.glyph.file.r;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileRustSVGGlyph extends SVGGlyph {

    public FileRustSVGGlyph() {
        super("/fx-svg/file/r/file-rust.svg");
    }

    public FileRustSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
