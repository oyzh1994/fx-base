package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class SettingSVGLabel extends SVGLabel {

    public SettingSVGLabel() {
        this.setUrl("/fx-plus/font/setting.svg");
    }

    public SettingSVGLabel(String size) {
        this();
        this.graphic().setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setText(BaseResourceBundle.getBaseString("setting"));
        this.setTipText(BaseResourceBundle.getBaseString("setting"));
        super.initNode();
    }
}
