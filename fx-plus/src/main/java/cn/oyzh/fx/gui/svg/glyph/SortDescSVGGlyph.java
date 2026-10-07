package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 降序排序 SVG 图标控件
 *
 * @author oyzh
 * @since 2024/4/10
 */
public class SortDescSVGGlyph extends SVGGlyph {

    /**
     * 构造降序排序 SVG 图标控件
     */
    public SortDescSVGGlyph() {
        super("/fx-svg/sort-descending.svg");
    }

    /**
     * 构造指定尺寸的降序排序 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public SortDescSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.sortDesc());
//        super.initNode();
//    }
}
