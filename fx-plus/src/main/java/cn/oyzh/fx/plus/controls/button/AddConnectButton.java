package cn.oyzh.fx.plus.controls.button;


import cn.oyzh.fx.plus.controls.svg.AddSVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * @author oyzh
 * @since 2024/04/08
 */
public class AddConnectButton extends IconButton {

    @Override
    public void initNode() {
        this.setPrefHeight(25);
        this.addClass("success");
        this.setText(I18nResourceBundle.i18nString("base.addConnect"));
        this.setTipText(I18nResourceBundle.i18nString("base.addConnect"));
        this.init(new AddSVGGlyph(), 0.7);
        super.initNode();
    }

}
