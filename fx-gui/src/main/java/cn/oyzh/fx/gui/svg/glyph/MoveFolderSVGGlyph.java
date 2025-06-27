package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class MoveFolderSVGGlyph extends SVGGlyph {

    public MoveFolderSVGGlyph() {
        this.setUrl("/fx-svg/move-folder.svg");
    }

    public MoveFolderSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
