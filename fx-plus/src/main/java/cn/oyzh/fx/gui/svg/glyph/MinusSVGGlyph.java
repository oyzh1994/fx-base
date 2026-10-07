package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 减号 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-11
 */
public class MinusSVGGlyph extends SVGGlyph {

    /**
     * 构造 SVG 图标控件
     */
    public MinusSVGGlyph() {
        super("/fx-svg/minus.svg");
    }

    /**
     * 构造指定尺寸的 SVG 图标控件
     *
     * @param size 尺寸
     */
    public MinusSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.minus());
//        super.initNode();
//    }
}
