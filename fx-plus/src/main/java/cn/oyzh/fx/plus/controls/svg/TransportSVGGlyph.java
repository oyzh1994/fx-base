package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class TransportSVGGlyph extends SVGGlyph {

    public TransportSVGGlyph() {
        this.setUrl("/fx-plus/font/arrow-left-right-line.svg");
    }

    public TransportSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(I18nResourceBundle.i18nString("base.transport"));
        super.initNode();
    }
}
