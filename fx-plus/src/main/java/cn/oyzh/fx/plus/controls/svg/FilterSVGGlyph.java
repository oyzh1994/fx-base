package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class FilterSVGGlyph extends SVGGlyph {

    public FilterSVGGlyph() {
        super("/fx-plus/font/filter.svg");
    }

    public FilterSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(BaseResourceBundle.getBaseString("filter"));
        super.initNode();
    }
}
