package cn.oyzh.fx.plus.controls.button;

import cn.oyzh.fx.plus.controls.svg.TransportSVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class TransportButton extends IconButton{

    @Override
    public void initNode() {
        this.setPrefHeight(25);
        this.setText(I18nResourceBundle.i18nString("base.transport"));
        this.setTipText(I18nResourceBundle.i18nString("base.transport"));
        this.init(new TransportSVGGlyph(), 0.7);
        super.initNode();
    }
}
