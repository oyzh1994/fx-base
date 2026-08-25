package cn.oyzh.fx.gui.svg.glyph.file.t;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileTomlSVGGlyph extends SVGGlyph {

    public FileTomlSVGGlyph() {
        super("/fx-svg/file/t/file-toml.svg");
    }

    public FileTomlSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
