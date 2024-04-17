package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class SortDescSVGGlyph extends SVGGlyph {

    public SortDescSVGGlyph() {
        this.setUrl("/fx-plus/font/sort-ascending.svg");
    }

    public SortDescSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(BaseResourceBundle.getBaseString("base.sortAsc"));
        super.initNode();
    }
}
