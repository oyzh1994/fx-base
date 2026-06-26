package cn.oyzh.fx.gui.svg.glyph.file.g;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileGroovySVGGlyph extends SVGGlyph {

    public FileGroovySVGGlyph() {
        super("/fx-svg/file/g/file-groovy.svg");
    }

    public FileGroovySVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
