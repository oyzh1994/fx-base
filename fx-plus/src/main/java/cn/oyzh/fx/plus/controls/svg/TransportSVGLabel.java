package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

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
        this.setText(I18nResourceBundle.i18nString("base.transport"));
        this.setTipText(I18nResourceBundle.i18nString("base.transport"));
        super.initNode();
    }
}
