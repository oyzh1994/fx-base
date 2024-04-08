package cn.oyzh.fx.plus.controls.button;


import cn.oyzh.fx.plus.i18n.I18nManager;

/**
 * 终端按钮
 *
 * @author oyzh
 * @since 2024/04/08
 */
public class TerminalButton extends IconButton {

    @Override
    public void initNode() {
        this.setPrefHeight(25);
        this.addClass("default");
        this.setText(I18nManager.baseI18nString("btn.terminal"));
        this.init("/fx-plus/font/code library.svg", 0.7);
        super.initNode();
    }
}
