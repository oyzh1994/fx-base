package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.I18nHelper;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class SortAscSVGGlyph extends SVGGlyph {

    public SortAscSVGGlyph() {
        this.setUrl("/fx-plus/font/sort-ascending.svg");
    }

    public SortAscSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(I18nHelper.sortAsc());
        super.initNode();
    }
}
