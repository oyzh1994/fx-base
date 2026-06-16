package cn.oyzh.fx.gui.svg.glyph.file.j;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileJsonSVGGlyph extends SVGGlyph {

    public FileJsonSVGGlyph() {
        super("/fx-svg/file/j/file-json.svg");
    }

    public FileJsonSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
