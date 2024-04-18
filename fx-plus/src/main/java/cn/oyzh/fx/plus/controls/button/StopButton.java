package cn.oyzh.fx.plus.controls.button;


import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * 取消按钮
 *
 * @author oyzh
 * @since 2020/10/29
 */
public class StopButton extends IconButton {

    @Override
    public void initNode() {
        this.setPrefHeight(25);
        this.addClass("default");
        this.setText(I18nResourceBundle.i18nString("base.stop"));
        this.setTipText(I18nResourceBundle.i18nString("base.stop"));
        this.init("/fx-plus/font/stop-circle-line.svg", 0.7);
        super.initNode();
    }
}
