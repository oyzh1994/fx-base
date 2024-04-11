package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class CopySVGLabel extends SVGLabel {

    public CopySVGLabel() {
        super("/fx-plus/font/copy.svg");
    }

    public CopySVGLabel(String size) {
        this();
        this.graphic().setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setText(BaseResourceBundle.getBaseString("copy"));
        this.setTipText(BaseResourceBundle.getBaseString("copy"));
        super.initNode();
    }
}
