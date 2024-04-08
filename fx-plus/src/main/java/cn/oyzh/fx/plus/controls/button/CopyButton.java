package cn.oyzh.fx.plus.controls.button;


import cn.oyzh.fx.plus.i18n.I18nManager;

/**
 * 复制按钮
 *
 * @author oyzh
 * @since 2024/04/08
 */
public class CopyButton extends IconButton {

    {
        this.setText(I18nManager.baseI18nString("btn.copy"));
        this.setPrefHeight(25);
        this.init("/fx-plus/font/copy.svg", 0.7);
    }
}
