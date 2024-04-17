package cn.oyzh.fx.plus.controls.button;


import cn.oyzh.fx.plus.i18n.BaseResourceBundle;
import cn.oyzh.fx.plus.i18n.I18nManager;

/**
 * 取消按钮
 *
 * @author oyzh
 * @since 2020/10/29
 */
public class SubmitButton extends IconButton {

    @Override
    public void initNode() {
        this.setPrefHeight(25);
        this.addClass("accent");
        this.setText(BaseResourceBundle.getBaseString("base.submit"));
        this.setTipText(BaseResourceBundle.getBaseString("base.submit"));
        this.init("/fx-plus/font/check.svg", 0.7);
        super.initNode();
    }
}
