package cn.oyzh.fx.plus.controls.button;


import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * 分组按钮
 *
 * @author oyzh
 * @since 2024/04/08
 */
public class AddGroupButton extends IconButton {

    @Override
    public void initNode() {
        this.setPrefHeight(25);
        this.addClass("accent");
        this.setText(BaseResourceBundle.getBaseString("base.addGroup"));
        this.setTipText(BaseResourceBundle.getBaseString("base.addGroup"));
        this.init("/fx-plus/font/addGroup.svg", 0.7);
        super.initNode();
    }
}
