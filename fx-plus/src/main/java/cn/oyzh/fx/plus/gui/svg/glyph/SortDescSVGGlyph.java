package cn.oyzh.fx.plus.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class SortDescSVGGlyph extends SVGGlyph {

    public SortDescSVGGlyph() {
        this.setUrl("/fx-plus/font/sort-descending.svg");
    }

    public SortDescSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(I18nHelper.sortDesc());
        super.initNode();
    }
}
