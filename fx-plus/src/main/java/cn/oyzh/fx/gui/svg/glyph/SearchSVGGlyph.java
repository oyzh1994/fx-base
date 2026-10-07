package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 搜索 SVG 图标控件
 *
 * @author oyzh
 * @since 2024/4/11
 */
public class SearchSVGGlyph extends SVGGlyph {

    /**
     * 构造搜索 SVG 图标控件
     */
    public SearchSVGGlyph() {
        super("/fx-svg/search.svg");
    }

    /**
     * 构造指定尺寸的搜索 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public SearchSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.search());
//        super.initNode();
//    }
}
