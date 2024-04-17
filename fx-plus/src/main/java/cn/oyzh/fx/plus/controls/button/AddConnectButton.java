package cn.oyzh.fx.plus.controls.button;


import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/04/08
 */
public class AddConnectButton extends IconButton {

    @Override
    public void initNode() {
        this.setPrefHeight(25);
        this.addClass("success");
        this.setText(BaseResourceBundle.getBaseString("base.addConnect"));
        this.setTipText(BaseResourceBundle.getBaseString("base.addConnect"));
        this.init("/fx-plus/font/add.svg", 0.7);
        super.initNode();
    }

}
