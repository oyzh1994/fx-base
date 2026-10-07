package cn.oyzh.fx.gui.svg.glyph.database;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 数据库函数 SVG 图标控件
 *
 * @author oyzh
 * @since 2024/4/10
 */
public class FunctionSVGGlyph extends SVGGlyph {

    /**
     * 构造数据库函数 SVG 图标控件
     */
    public FunctionSVGGlyph() {
        super("/fx-svg/database/function.svg");
    }

    /**
     * 构造指定尺寸的数据库函数 SVG 图标控件
     *
     * @param size 尺寸
     */
    public FunctionSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.function());
//        super.initNode();
//    }
}
