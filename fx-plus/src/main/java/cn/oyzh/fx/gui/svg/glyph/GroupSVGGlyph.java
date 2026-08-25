package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.font.FontManager;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class GroupSVGGlyph extends SVGGlyph {

    public GroupSVGGlyph() {
        super("/fx-svg/group.svg");
    }

    public GroupSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
