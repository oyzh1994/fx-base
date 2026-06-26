package cn.oyzh.fx.gui.svg.glyph.file.j;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileJspSVGGlyph extends SVGGlyph {

    public FileJspSVGGlyph() {
        super("/fx-svg/file/j/file-jsp.svg");
    }

    public FileJspSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
