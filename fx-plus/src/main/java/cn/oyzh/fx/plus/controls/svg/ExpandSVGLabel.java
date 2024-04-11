package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class ExpandSVGLabel extends SVGLabel {

    public ExpandSVGLabel() {
        this.setUrl("/fx-plus/font/arrow-to-right.svg");
    }

    public ExpandSVGLabel(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setText(BaseResourceBundle.getBaseString("expand"));
        this.setTipText(BaseResourceBundle.getBaseString("expand"));
        super.initNode();
    }
}
