package cn.oyzh.fx.plus.controls.button;


/**
 * 取消按钮
 *
 * @author oyzh
 * @since 2020/10/29
 */
public class SubmitButton extends IconButton {

    {
        this.setText("提交");
        this.setPrefHeight(25);
        this.getStyleClass().add("accent");
        this.init("/fx-plus/font/check.svg", 0.7);
    }
}
