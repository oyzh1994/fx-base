package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class SettingSVGLabel extends SVGLabel {

    @Override
    public void initNode() {
        this.setUrl("/fx-plus/font/setting.svg");
        this.setText(BaseResourceBundle.getBaseString("setting"));
        this.setTipText(BaseResourceBundle.getBaseString("setting"));
        super.initNode();
    }
}
