package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.I18nHelper;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class QRCodeSVGGlyph extends SVGGlyph {

    public QRCodeSVGGlyph() {
        this.setUrl("/fx-plus/font/qrcode.svg");
    }

    public QRCodeSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(I18nHelper.qrCode());
        super.initNode();
    }
}
