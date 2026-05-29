package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class FolderSVGGlyph extends SVGGlyph {

    public FolderSVGGlyph() {
        super("/fx-svg/folder.svg");
    }

    public FolderSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
