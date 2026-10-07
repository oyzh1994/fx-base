package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 上一个 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-10
 */
public class PrevSVGGlyph extends SVGGlyph {

    /**
     * 构造上一个 SVG 图标控件
     */
    public PrevSVGGlyph() {
        super("/fx-svg/direction-up.svg");
    }

    /**
     * 构造指定尺寸的上一个 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public PrevSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.prev());
//        super.initNode();
//    }
}
