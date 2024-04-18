package cn.oyzh.fx.plus.controls.button;


import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * 取消按钮
 *
 * @author oyzh
 * @since 2020/10/29
 */
public class SaveButton extends IconButton {

    @Override
    public void initNode() {
        this.setPrefHeight(25);
        this.addClass("accent");
        this.setText(I18nResourceBundle.i18nString("base.save"));
        this.setTipText(I18nResourceBundle.i18nString("base.save"));
        this.init("/fx-plus/font/save.svg", 0.7);
        super.initNode();
    }
}
