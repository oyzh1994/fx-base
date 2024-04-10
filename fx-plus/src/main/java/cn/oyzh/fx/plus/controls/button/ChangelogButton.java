package cn.oyzh.fx.plus.controls.button;


import cn.oyzh.fx.plus.i18n.BaseResourceBundle;
import cn.oyzh.fx.plus.i18n.I18nManager;

/**
 * 更新日志按钮
 *
 * @author oyzh
 * @since 2024/04/08
 */
public class ChangelogButton extends IconButton {

    @Override
    public void initNode() {
        this.setPrefHeight(25);
        this.addClass("danger");
        this.setText(BaseResourceBundle.getBaseString("btn.changelog"));
        this.init("/fx-plus/font/changelog.svg", 0.7);
        super.initNode();
    }
}
