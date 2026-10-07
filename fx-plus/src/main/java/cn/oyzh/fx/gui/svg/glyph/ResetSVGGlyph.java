package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 重置 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-11
 */
public class ResetSVGGlyph extends SVGGlyph {

    /**
     * 构造重置 SVG 图标控件
     */
    public ResetSVGGlyph() {
        super("/fx-svg/reset.svg");
    }

    /**
     * 构造指定尺寸的重置 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public ResetSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.reset());
//        super.initNode();
//    }
}
