package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 新增主机 SVG 图标控件
 *
 * @author oyzh
 * @since 2024/4/11
 */
public class AddHostSVGGlyph extends SVGGlyph {

    /**
     * 构造 SVG 图标控件
     */
    public AddHostSVGGlyph() {
        super("/fx-svg/addHost.svg");
    }

    /**
     * 构造指定尺寸的 SVG 图标控件
     *
     * @param size 尺寸
     */
    public AddHostSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
