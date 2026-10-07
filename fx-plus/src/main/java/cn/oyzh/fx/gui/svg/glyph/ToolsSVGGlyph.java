package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 工具 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-11
 */
public class ToolsSVGGlyph extends SVGGlyph {

    /**
     * 构造工具 SVG 图标控件
     */
    public ToolsSVGGlyph() {
        super("/fx-svg/tools.svg");
    }

    /**
     * 构造指定尺寸的工具 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public ToolsSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
