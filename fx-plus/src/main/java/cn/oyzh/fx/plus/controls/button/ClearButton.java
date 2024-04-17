package cn.oyzh.fx.plus.controls.button;


import cn.oyzh.fx.plus.i18n.BaseResourceBundle;
import cn.oyzh.fx.plus.i18n.I18nManager;

/**
 * 重置按钮
 *
 * @author oyzh
 * @since 2024/04/08
 */
public class ClearButton extends IconButton {

    @Override
    public void initNode() {
        this.setPrefHeight(25);
        this.addClass("danger");
        this.setText(BaseResourceBundle.getBaseString("base.clear"));
        this.setTipText(BaseResourceBundle.getBaseString("base.clear"));
        this.init("/fx-plus/font/clear.svg", 0.7);
        super.initNode();
    }
}
