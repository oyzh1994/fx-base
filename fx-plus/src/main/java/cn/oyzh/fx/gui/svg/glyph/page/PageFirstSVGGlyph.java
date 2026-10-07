package cn.oyzh.fx.gui.svg.glyph.page;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 首页 SVG 图标控件
 *
 * @author oyzh
 * @since 2024/08/06
 */
public class PageFirstSVGGlyph extends SVGGlyph {

    /**
     * 构造首页 SVG 图标控件
     */
    public PageFirstSVGGlyph() {
        super("/fx-svg/page/page-first.svg");
    }

    /**
     * 构造指定尺寸的首页 SVG 图标控件
     *
     * @param size 尺寸
     */
    public PageFirstSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.firstPage());
//        super.initNode();
//    }
}
