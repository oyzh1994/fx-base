package cn.oyzh.fx.plus.controls.button;


import cn.oyzh.fx.plus.i18n.I18nManager;

/**
 * 重置按钮
 *
 * @author oyzh
 * @since 2024/04/08
 */
public class ResetButton extends IconButton {

    {
        this.setText(I18nManager.baseI18nString("btn.reset"));
        this.setPrefHeight(25);
        this.init("/fx-plus/font/reset.svg", 0.7);
    }
}
