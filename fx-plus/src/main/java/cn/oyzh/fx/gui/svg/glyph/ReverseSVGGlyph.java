package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 反转 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-10
 */
public class ReverseSVGGlyph extends SVGGlyph {

    /**
     * 构造反转 SVG 图标控件
     */
    public ReverseSVGGlyph() {
        super("/fx-svg/reverse.svg");
    }

    /**
     * 构造指定尺寸的反转 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public ReverseSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.reverse());
//        super.initNode();
//    }
}
