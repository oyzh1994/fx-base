package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 右侧 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-09-05
 */
public class RightSideSVGGlyph extends SVGGlyph {

    /**
     * 构造右侧 SVG 图标控件
     */
    public RightSideSVGGlyph() {
        super("/fx-svg/rightside.svg");
    }

    /**
     * 构造指定尺寸的右侧 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public RightSideSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.add());
//        super.initNode();
//    }
}
