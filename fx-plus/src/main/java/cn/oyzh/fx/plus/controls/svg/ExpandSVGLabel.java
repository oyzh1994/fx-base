package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class ExpandSVGLabel extends SVGLabel {

    public ExpandSVGLabel() {
        this.setGraphic(new ExpendSVGGlyph());
    }

    public ExpandSVGLabel(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setText(BaseResourceBundle.getBaseString("base.expand"));
        this.setTipText(BaseResourceBundle.getBaseString("base.expand"));
        super.initNode();
    }
}
