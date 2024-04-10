package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class TransportSVGLabel extends SVGLabel {

    @Override
    public void initNode() {
        this.setUrl("/fx-plus/font/arrow-left-right-line.svg");
        this.setText(BaseResourceBundle.getBaseString("transport"));
        this.setTipText(BaseResourceBundle.getBaseString("transport"));
        super.initNode();
    }
}
