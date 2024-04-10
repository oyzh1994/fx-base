package cn.oyzh.fx.plus.controls.button;


import cn.oyzh.fx.plus.i18n.BaseResourceBundle;
import cn.oyzh.fx.plus.i18n.I18nManager;

/**
 * 测试按钮
 *
 * @author oyzh
 * @since 2024/04/10
 */
public class TestButton extends IconButton {

    @Override
    public void initNode() {
        this.setPrefHeight(25);
        this.addClass("success");
        this.setText(BaseResourceBundle.getBaseString("test"));
        this.setTipText(BaseResourceBundle.getBaseString("test"));
        this.init("/fx-plus/font/link.svg", 0.7);
        super.initNode();
    }
}
