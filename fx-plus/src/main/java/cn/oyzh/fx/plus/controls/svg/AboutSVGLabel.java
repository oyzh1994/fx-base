package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class AboutSVGLabel extends SVGLabel {

    @Override
    public void initNode() {
        this.setUrl("/fx-plus/font/info-circle.svg");
        this.setText(BaseResourceBundle.getBaseString("about"));
        this.setTipText(BaseResourceBundle.getBaseString("about"));
        super.initNode();
    }
}
