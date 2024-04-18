package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class FilterSVGLabel extends SVGLabel{

    public FilterSVGLabel() {
        this.setGraphic(new FilterSVGGlyph());
    }

    public FilterSVGLabel(String size) {
        this();
        this.graphic().setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setText(I18nResourceBundle.i18nString("base.filter"));
        this.setTipText(I18nResourceBundle.i18nString("base.filter"));
        super.initNode();
    }
}
