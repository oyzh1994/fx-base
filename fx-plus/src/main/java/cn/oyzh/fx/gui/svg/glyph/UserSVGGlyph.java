package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class UserSVGGlyph extends SVGGlyph {

    public UserSVGGlyph() {
        super("/fx-svg/user.svg");
    }

    public UserSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
