package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class NextSVGGlyph extends SVGGlyph {

    public NextSVGGlyph() {
        this.setUrl("/fx-plus/font/direction-down.svg");
    }

    public NextSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(I18nResourceBundle.i18nString("base.next"));
        super.initNode();
    }
}
