package cn.oyzh.fx.plus.controls.button;


/**
 * @author oyzh
 * @since 2020/10/29
 */
public class CancelButton extends IconButton {

    {
        this.setText("取消");
        this.setPrefHeight(25);
        this.getStyleClass().add("danger");
        this.init("/fx-plus/font/close.svg", 0.7);
        this.setOnAction(actionEvent -> this.getScene().getWindow().hide());
    }
}
