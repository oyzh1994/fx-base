package cn.oyzh.fx.gui.svg.glyph.page;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 上一页 SVG 图标控件
 *
 * @author oyzh
 * @since 2024/08/06
 */
public class PagePrevSVGGlyph extends SVGGlyph {

    /**
     * 构造上一页 SVG 图标控件
     */
    public PagePrevSVGGlyph() {
        super("/fx-svg/page/page-prev.svg");
    }

    /**
     * 构造指定尺寸的上一页 SVG 图标控件
     *
     * @param size 尺寸
     */
    public PagePrevSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.prevPage());
//        super.initNode();
//    }
}
