package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 旧版本 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-11
 */
public class OldSVGGlyph extends SVGGlyph {

    /**
     * 构造旧版本 SVG 图标控件
     */
    public OldSVGGlyph() {
        super("/fx-svg/old.svg");
    }

    /**
     * 构造指定尺寸的旧版本 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public OldSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
