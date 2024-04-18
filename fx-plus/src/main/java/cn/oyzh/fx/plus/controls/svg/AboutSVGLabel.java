package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class AboutSVGLabel extends SVGLabel {

    public AboutSVGLabel() {
        this.setUrl("/fx-plus/font/info-circle.svg");
    }

    public AboutSVGLabel(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setText(I18nResourceBundle.i18nString("base.about"));
        this.setTipText(I18nResourceBundle.i18nString("base.about"));
        super.initNode();
    }
}
