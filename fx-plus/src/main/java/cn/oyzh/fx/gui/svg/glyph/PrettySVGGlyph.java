package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 格式化 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-11
 */
public class PrettySVGGlyph extends SVGGlyph {

    /**
     * 构造格式化 SVG 图标控件
     */
    public PrettySVGGlyph() {
        super("/fx-svg/pretty.svg");
    }

    /**
     * 构造指定尺寸的格式化 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public PrettySVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.pretty());
//        super.initNode();
//    }
}
