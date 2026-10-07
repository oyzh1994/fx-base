package cn.oyzh.fx.gui.svg.glyph.page;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 分页设置 SVG 图标控件
 *
 * @author oyzh
 * @since 2024/08/06
 */
public class PageSettingSVGGlyph extends SVGGlyph {

    /**
     * 构造分页设置 SVG 图标控件
     */
    public PageSettingSVGGlyph() {
        super("/fx-svg/page/page-setting3.svg");
    }

    /**
     * 构造指定尺寸的分页设置 SVG 图标控件
     *
     * @param size 尺寸
     */
    public PageSettingSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.setting());
//        super.initNode();
//    }
}
