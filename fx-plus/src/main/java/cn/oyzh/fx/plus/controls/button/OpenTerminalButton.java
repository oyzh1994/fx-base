package cn.oyzh.fx.plus.controls.button;


import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * 终端按钮
 *
 * @author oyzh
 * @since 2024/04/08
 */
public class OpenTerminalButton extends IconButton {

    @Override
    public void initNode() {
        this.setPrefHeight(25);
        this.addClass("default");
        this.setText(I18nResourceBundle.i18nString("base.openTerminal"));
        this.setTipText(I18nResourceBundle.i18nString("base.openTerminal"));
        this.init("/fx-plus/font/code library.svg", 0.7);
        super.initNode();
    }
}
