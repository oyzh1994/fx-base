package cn.oyzh.fx.plus.controls.button;


import cn.oyzh.fx.plus.i18n.I18nManager;

/**
 * 分组按钮
 *
 * @author oyzh
 * @since 2024/04/08
 */
public class GroupButton extends IconButton {

    @Override
    public void initNode() {
        this.setPrefHeight(25);
        this.addClass("accent");
        this.setText(I18nManager.baseI18nString("btn.group"));
        this.init("/fx-plus/font/addGroup.svg", 0.7);
        super.initNode();
    }
}
