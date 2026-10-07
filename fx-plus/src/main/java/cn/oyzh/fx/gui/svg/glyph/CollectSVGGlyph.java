package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 收藏 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-10
 */
public class CollectSVGGlyph extends SVGGlyph {

    /**
     * 构造 SVG 图标控件
     */
    public CollectSVGGlyph() {
        super("/fx-svg/star-l.svg");
    }

    /**
     * 构造指定尺寸的 SVG 图标控件
     *
     * @param size 尺寸
     */
    public CollectSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.collect());
//        super.initNode();
//    }
}
