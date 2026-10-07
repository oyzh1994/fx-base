package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 展开 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-10
 */
public class ExpendSVGGlyph extends SVGGlyph {

    /**
     * 构造 SVG 图标控件
     */
    public ExpendSVGGlyph() {
        super("/fx-svg/arrow-to-right.svg");
    }

    /**
     * 构造指定尺寸的 SVG 图标控件
     *
     * @param size 尺寸
     */
    public ExpendSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.expand());
//        super.initNode();
//    }
}
