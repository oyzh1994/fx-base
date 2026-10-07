package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 下一个 SVG 图标控件
 *
 * @author oyzh
 * @since 2024/4/10
 */
public class NextSVGGlyph extends SVGGlyph {

    /**
     * 构造下一个 SVG 图标控件
     */
    public NextSVGGlyph() {
        super("/fx-svg/direction-down.svg");
    }

    /**
     * 构造指定尺寸的下一个 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public NextSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.next());
//        super.initNode();
//    }
}
