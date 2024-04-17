package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class TransportSVGLabel extends SVGLabel {

    public TransportSVGLabel() {
        this.setGraphic(new TransportSVGGlyph());
    }

    public TransportSVGLabel(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setText(BaseResourceBundle.getBaseString("base.transport"));
        this.setTipText(BaseResourceBundle.getBaseString("base.transport"));
        super.initNode();
    }
}
