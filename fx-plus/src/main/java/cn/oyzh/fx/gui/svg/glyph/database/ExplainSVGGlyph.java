package cn.oyzh.fx.gui.svg.glyph.database;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 执行计划 SVG 图标控件
 *
 * @author oyzh
 * @since 2024/08/16
 */
public class ExplainSVGGlyph extends SVGGlyph {

    /**
     * 构造执行计划 SVG 图标控件
     */
    public ExplainSVGGlyph() {
        super("/fx-svg/database/explain.svg");
    }

    /**
     * 构造指定尺寸的执行计划 SVG 图标控件
     *
     * @param size 尺寸
     */
    public ExplainSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.explain());
//        super.initNode();
//    }
}
