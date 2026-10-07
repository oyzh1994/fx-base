package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 缩小 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-11
 */
public class ReduceSVGGlyph extends SVGGlyph {

    /**
     * 构造缩小 SVG 图标控件
     */
    public ReduceSVGGlyph() {
        super("/fx-svg/reduce.svg");
    }

    /**
     * 构造指定尺寸的缩小 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public ReduceSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.reduce());
//        super.initNode();
//    }
}
