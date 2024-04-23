package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class SubmitSVGGlyph extends SVGGlyph {

    public SubmitSVGGlyph() {
        this.setUrl("/fx-plus/font/check.svg");
    }

    public SubmitSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(I18nResourceBundle.i18nString("base.submit"));
        super.initNode();
    }
}
