package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class RefreshSVGGlyph extends SVGGlyph {

    public RefreshSVGGlyph() {
        this.setUrl("/fx-plus/font/reload.svg");
    }

    public RefreshSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(I18nResourceBundle.i18nString("base.refresh"));
        super.initNode();
    }
}
