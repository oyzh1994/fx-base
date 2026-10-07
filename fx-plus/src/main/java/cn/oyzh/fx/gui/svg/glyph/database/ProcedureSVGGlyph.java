package cn.oyzh.fx.gui.svg.glyph.database;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 存储过程 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-11
 */
public class ProcedureSVGGlyph extends SVGGlyph {

    /**
     * 构造存储过程 SVG 图标控件
     */
    public ProcedureSVGGlyph() {
        super("/fx-svg/database/procedure.svg");
    }

    /**
     * 构造指定尺寸的存储过程 SVG 图标控件
     *
     * @param size 尺寸
     */
    public ProcedureSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.procedure());
//        super.initNode();
//    }
}
