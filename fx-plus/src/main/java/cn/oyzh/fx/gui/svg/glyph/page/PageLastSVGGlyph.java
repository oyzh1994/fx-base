package cn.oyzh.fx.gui.svg.glyph.page;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 末页 SVG 图标控件
 *
 * @author oyzh
 * @since 2024/08/06
 */
public class PageLastSVGGlyph extends SVGGlyph {

    /**
     * 构造末页 SVG 图标控件
     */
    public PageLastSVGGlyph() {
        super("/fx-svg/page/page-last.svg");
    }

    /**
     * 构造指定尺寸的末页 SVG 图标控件
     *
     * @param size 尺寸
     */
    public PageLastSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.lastPage());
//        super.initNode();
//    }
}
