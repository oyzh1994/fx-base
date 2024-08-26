package cn.oyzh.fx.plus.controls.svg.database;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/08/26
 */
public class DumpSVGGlyph extends SVGGlyph {

    public DumpSVGGlyph() {
        this.setUrl("/fx-plus/font/database/dump.svg");
    }

    public DumpSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(I18nHelper.dump());
        super.initNode();
    }
}
