package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class CollapseSVGLabel extends SVGLabel {

    public CollapseSVGLabel() {
        this.setUrl("/fx-plus/font/left-arrow-to-left.svg");
    }

    public CollapseSVGLabel(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setText(BaseResourceBundle.getBaseString("collapse"));
        this.setTipText(BaseResourceBundle.getBaseString("collapse"));
        super.initNode();
    }
}
