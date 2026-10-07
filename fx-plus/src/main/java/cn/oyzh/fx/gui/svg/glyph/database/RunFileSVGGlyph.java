package cn.oyzh.fx.gui.svg.glyph.database;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 运行文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2024/08/29
 */
public class RunFileSVGGlyph extends SVGGlyph {

    /**
     * 构造运行文件 SVG 图标控件
     */
    public RunFileSVGGlyph() {
        super("/fx-svg/database/run-file.svg");
    }

    /**
     * 构造指定尺寸的运行文件 SVG 图标控件
     *
     * @param size 尺寸
     */
    public RunFileSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.runSqlFile());
//        super.initNode();
//    }
}
