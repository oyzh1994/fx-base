package cn.oyzh.fx.gui.svg.glyph.file.g;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileGoSVGGlyph extends SVGGlyph {

    public FileGoSVGGlyph() {
        super("/fx-svg/file/g/file-go.svg");
    }

    public FileGoSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
