package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class QuitSVGLabel extends SVGLabel{

    @Override
    public void initNode() {
        this.setUrl("/fx-plus/font/poweroff.svg");
        this.setText(BaseResourceBundle.getBaseString("quit"));
        this.setTipText(BaseResourceBundle.getBaseString("quit"));
        super.initNode();
    }
}
