package cn.oyzh.fx.gui.svg.glyph.database;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/08/29
 */
public class RunFileSVGGlyph extends SVGGlyph {

    public RunFileSVGGlyph() {
        super("/fx-svg/database/run-file.svg");
    }

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
