package cn.oyzh.fx.plus.controls.button;


import cn.oyzh.fx.plus.i18n.BaseResourceBundle;
import cn.oyzh.fx.plus.i18n.I18nManager;

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
        this.setText(BaseResourceBundle.getBaseString("btn.stop"));
        this.init("/fx-plus/font/stop-circle-line.svg", 0.7);
        super.initNode();
    }
}
