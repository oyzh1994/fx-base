package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 用户 SVG 图标控件
 *
 * @author oyzh
 * @since 2024/4/11
 */
public class UserSVGGlyph extends SVGGlyph {

    /**
     * 构造用户 SVG 图标控件
     */
    public UserSVGGlyph() {
        super("/fx-svg/user.svg");
    }

    /**
     * 构造指定尺寸的用户 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public UserSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
