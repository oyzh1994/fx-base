package cn.oyzh.fx.gui.svg.glyph.file.x;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileXzSVGGlyph extends SVGGlyph {

    public FileXzSVGGlyph() {
        super("/fx-svg/file/x/file-xz.svg");
    }

    public FileXzSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
