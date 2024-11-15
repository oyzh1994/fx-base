package cn.oyzh.fx.plus.gui.svg.glyph.database;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/08/29
 */
public class RunSqlFileSVGGlyph extends SVGGlyph {

    public RunSqlFileSVGGlyph() {
        this.setUrl("/fx-plus/font/database/runSqlFile.svg");
    }

    public RunSqlFileSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(I18nHelper.runSqlFile());
        super.initNode();
    }
}
