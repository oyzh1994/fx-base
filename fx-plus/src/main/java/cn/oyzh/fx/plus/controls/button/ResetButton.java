package cn.oyzh.fx.plus.controls.button;


import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * 重置按钮
 *
 * @author oyzh
 * @since 2024/04/08
 */
public class ResetButton extends IconButton {

    @Override
    public void initNode() {
        this.setPrefHeight(25);
        this.setText(I18nResourceBundle.i18nString("base.reset"));
        this.setTipText(I18nResourceBundle.i18nString("base.reset"));
        this.init("/fx-plus/font/reset.svg", 0.7);
        super.initNode();
    }
}
