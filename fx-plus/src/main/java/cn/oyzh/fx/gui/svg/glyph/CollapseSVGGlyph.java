package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 折叠 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-10
 */
public class CollapseSVGGlyph extends SVGGlyph {

    /**
     * 构造 SVG 图标控件
     */
    public CollapseSVGGlyph() {
        super("/fx-svg/left-arrow-to-left.svg");
    }

    /**
     * 构造指定尺寸的 SVG 图标控件
     *
     * @param size 尺寸
     */
    public CollapseSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.collapse());
//        super.initNode();
//    }
}
