package cn.oyzh.fx.gui.svg.glyph.page;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 下一页 SVG 图标控件
 *
 * @author oyzh
 * @since 2024/08/06
 */
public class PageNextSVGGlyph extends SVGGlyph {

    /**
     * 构造下一页 SVG 图标控件
     */
    public PageNextSVGGlyph() {
        super("/fx-svg/page/page-next.svg");
    }

    /**
     * 构造指定尺寸的下一页 SVG 图标控件
     *
     * @param size 尺寸
     */
    public PageNextSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.nextPage());
//        super.initNode();
//    }
}
