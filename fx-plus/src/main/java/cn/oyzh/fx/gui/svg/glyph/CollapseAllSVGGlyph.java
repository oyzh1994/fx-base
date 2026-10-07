package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 全部折叠 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-10
 */
public class CollapseAllSVGGlyph extends SVGGlyph {

    /**
     * 构造 SVG 图标控件
     */
    public CollapseAllSVGGlyph() {
        super("/fx-svg/vertical-align-middl.svg");
    }

    /**
     * 构造指定尺寸的 SVG 图标控件
     *
     * @param size 尺寸
     */
    public CollapseAllSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.collapseAll());
//        super.initNode();
//    }
}
