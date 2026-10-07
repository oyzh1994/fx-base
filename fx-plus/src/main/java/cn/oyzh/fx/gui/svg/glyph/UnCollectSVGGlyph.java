package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 取消收藏 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-10
 */
public class UnCollectSVGGlyph extends SVGGlyph {

    /**
     * 构造取消收藏 SVG 图标控件
     */
    public UnCollectSVGGlyph() {
        super("/fx-svg/star.svg");
    }

    /**
     * 构造指定尺寸的取消收藏 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public UnCollectSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.unCollect());
//        super.initNode();
//    }
}
