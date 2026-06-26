package cn.oyzh.fx.gui.svg.glyph.file.c;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileConfigSVGGlyph extends SVGGlyph {

    public FileConfigSVGGlyph() {
        super("/fx-svg/file/c/file-config.svg");
    }

    public FileConfigSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
