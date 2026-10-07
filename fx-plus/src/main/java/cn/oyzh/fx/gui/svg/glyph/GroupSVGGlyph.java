package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.font.FontManager;

/**
 * 分组 SVG 图标控件
 *
 * @author oyzh
 * @since 2024/4/10
 */
public class GroupSVGGlyph extends SVGGlyph {

    /**
     * 构造 SVG 图标控件
     */
    public GroupSVGGlyph() {
        super("/fx-svg/group.svg");
    }

    /**
     * 构造指定尺寸的 SVG 图标控件
     *
     * @param size 尺寸
     */
    public GroupSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
