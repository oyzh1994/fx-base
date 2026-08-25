package cn.oyzh.fx.gui.svg.glyph.file.r;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileRarSVGGlyph extends SVGGlyph {

    public FileRarSVGGlyph() {
        super("/fx-svg/file/r/file-rar.svg");
    }

    public FileRarSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
