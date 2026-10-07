package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 运行 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-08-12
 */
public class RunSVGGlyph extends SVGGlyph {

    /**
     * 构造运行 SVG 图标控件
     */
    public RunSVGGlyph() {
        super("/fx-svg/run-solid.svg");
    }

    /**
     * 构造指定尺寸的运行 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public RunSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.add());
//        super.initNode();
//    }
}
