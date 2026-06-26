package cn.oyzh.fx.gui.svg.glyph.file.l;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileLuaSVGGlyph extends SVGGlyph {

    public FileLuaSVGGlyph() {
        super("/fx-svg/file/l/file-lua.svg");
    }

    public FileLuaSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
