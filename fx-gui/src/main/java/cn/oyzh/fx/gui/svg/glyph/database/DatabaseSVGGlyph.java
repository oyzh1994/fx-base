package cn.oyzh.fx.gui.svg.glyph.database;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/09/05
 */
public class DatabaseSVGGlyph extends SVGGlyph {

    public DatabaseSVGGlyph() {
        this.setUrl("/fx-plus/font/database/database.svg");
    }

    public DatabaseSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(I18nHelper.dump());
        super.initNode();
    }
}
