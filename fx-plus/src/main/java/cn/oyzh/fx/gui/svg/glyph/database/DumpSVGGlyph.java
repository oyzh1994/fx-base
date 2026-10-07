package cn.oyzh.fx.gui.svg.glyph.database;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 数据库导出 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-08-16
 */
public class DumpSVGGlyph extends SVGGlyph {

    /**
     * 构造数据库导出 SVG 图标控件
     */
    public DumpSVGGlyph() {
        super("/fx-svg/database/dump.svg");
    }

    /**
     * 构造指定尺寸的数据库导出 SVG 图标控件
     *
     * @param size 尺寸
     */
    public DumpSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.dump());
//        super.initNode();
//    }
}
