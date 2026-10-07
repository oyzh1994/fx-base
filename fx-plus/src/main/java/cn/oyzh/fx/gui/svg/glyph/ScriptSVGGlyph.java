package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 脚本 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-11
 */
public class ScriptSVGGlyph extends SVGGlyph {

    /**
     * 构造脚本 SVG 图标控件
     */
    public ScriptSVGGlyph() {
        super("/fx-svg/script.svg");
    }

    /**
     * 构造指定尺寸的脚本 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public ScriptSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

}
