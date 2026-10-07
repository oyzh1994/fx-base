package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 下一步 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-08-26
 */
public class NextStepSVGGlyph extends SVGGlyph {

    /**
     * 构造下一步 SVG 图标控件
     */
    public NextStepSVGGlyph() {
        super("/fx-svg/next-step.svg");
    }

    /**
     * 构造指定尺寸的下一步 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public NextStepSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.nextStep());
//        super.initNode();
//    }
}
