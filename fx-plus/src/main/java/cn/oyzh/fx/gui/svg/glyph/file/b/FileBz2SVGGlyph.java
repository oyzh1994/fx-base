package cn.oyzh.fx.gui.svg.glyph.file.b;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileBz2SVGGlyph extends SVGGlyph {

    public FileBz2SVGGlyph() {
        super("/fx-svg/file/b/file-bz2.svg");
    }

    public FileBz2SVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
