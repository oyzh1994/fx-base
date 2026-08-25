package cn.oyzh.fx.gui.svg.glyph.file.y;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileYmlSVGGlyph extends SVGGlyph {

    public FileYmlSVGGlyph() {
        super("/fx-svg/file/y/file-yml.svg");
    }

    public FileYmlSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
