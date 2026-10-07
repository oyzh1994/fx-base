package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 设计 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-08-01
 */
public class DesignSVGGlyph extends SVGGlyph {

    /**
     * 构造 SVG 图标控件
     */
    public DesignSVGGlyph() {
        super("/fx-svg/design.svg");
    }

    /**
     * 构造指定尺寸的 SVG 图标控件
     *
     * @param size 尺寸
     */
    public DesignSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.design());
//        super.initNode();
//    }
}
