package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 上一步 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-08-26
 */
public class PrevStepSVGGlyph extends SVGGlyph {

    /**
     * 构造上一步 SVG 图标控件
     */
    public PrevStepSVGGlyph() {
        super("/fx-svg/prev-step.svg");
    }

    /**
     * 构造指定尺寸的上一步 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public PrevStepSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.prevStep());
//        super.initNode();
//    }
}
