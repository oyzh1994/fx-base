package cn.oyzh.fx.plus.controls.textarea;

/**
 * @author oyzh
 * @since 2023/11/14
 */
public class DisabledTextArea extends FlexTextArea {

    {
        this.setDisable(true);
        this.setRequire(false);
        this.setEditable(false);
        this.setPickOnBounds(true);
        this.setFocusTraversable(false);
    }
}
