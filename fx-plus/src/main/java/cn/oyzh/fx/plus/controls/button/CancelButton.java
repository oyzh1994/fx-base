package cn.oyzh.fx.plus.controls.button;


import cn.oyzh.fx.plus.i18n.I18nManager;

/**
 * @author oyzh
 * @since 2020/10/29
 */
public class CancelButton extends IconButton {

    {
        this.setText(I18nManager.baseI18nString("btn.cancel"));
        this.setPrefHeight(25);
        this.getStyleClass().add("danger");
        this.init("/fx-plus/font/close.svg", 0.7);
        this.setOnAction(actionEvent -> this.getScene().getWindow().hide());
    }
}
