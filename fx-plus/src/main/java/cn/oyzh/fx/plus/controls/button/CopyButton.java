package cn.oyzh.fx.plus.controls.button;


import cn.oyzh.fx.plus.i18n.BaseResourceBundle;
import cn.oyzh.fx.plus.i18n.I18nManager;

/**
 * 复制按钮
 *
 * @author oyzh
 * @since 2024/04/08
 */
public class CopyButton extends IconButton {

    @Override
    public void initNode() {
        this.setPrefHeight(25);
        this.setText(BaseResourceBundle.getBaseString("base.copy"));
        this.setTipText(BaseResourceBundle.getBaseString("base.copy"));
        this.init("/fx-plus/font/copy.svg", 0.7);
        super.initNode();
    }
}
