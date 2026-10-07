package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 放大 SVG 图标控件
 *
 * @author oyzh
 * @since 2024/7/9
 */
public class EnlargeSVGGlyph extends SVGGlyph {

    /**
     * 构造 SVG 图标控件
     */
    public EnlargeSVGGlyph() {
        super("/fx-svg/enlarge.svg");
    }

    /**
     * 构造指定尺寸的 SVG 图标控件
     *
     * @param size 尺寸
     */
    public EnlargeSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.edit());
//        super.initNode();
//    }
}
