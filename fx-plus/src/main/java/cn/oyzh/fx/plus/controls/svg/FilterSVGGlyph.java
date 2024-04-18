package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class FilterSVGGlyph extends SVGGlyph {

    public FilterSVGGlyph() {
        this.setUrl("/fx-plus/font/filter.svg");
    }

    public FilterSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(I18nResourceBundle.i18nString("base.filter"));
        super.initNode();
    }
}
