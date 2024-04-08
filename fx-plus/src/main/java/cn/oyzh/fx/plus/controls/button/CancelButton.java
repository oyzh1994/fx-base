package cn.oyzh.fx.plus.controls.button;


import cn.oyzh.fx.plus.i18n.I18nManager;

/**
 * @author oyzh
 * @since 2020/10/29
 */
public class CancelButton extends IconButton {

    @Override
    public void initNode() {
        this.setPrefHeight(25);
        this.addClass("danger");
        this.setText(I18nManager.baseI18nString("btn.cancel"));
        this.init("/fx-plus/font/close.svg", 0.7);
        this.setOnAction(actionEvent -> this.getScene().getWindow().hide());
        super.initNode();
    }
}
