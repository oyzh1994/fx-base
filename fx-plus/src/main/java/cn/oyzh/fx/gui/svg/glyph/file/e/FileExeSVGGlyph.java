package cn.oyzh.fx.gui.svg.glyph.file.e;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-06
 */
public class FileExeSVGGlyph extends SVGGlyph {

    public FileExeSVGGlyph() {
        super("/fx-svg/file/e/file-exe.svg");
    }

    public FileExeSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
