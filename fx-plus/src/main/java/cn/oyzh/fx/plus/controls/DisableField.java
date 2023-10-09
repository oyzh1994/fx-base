package cn.oyzh.fx.plus.controls;

/**
 * 禁用文本控件
 *
 * @author oyzh
 * @since 2020/10/29
 */
public class DisableField extends BaseTextField {

    {
        this.setDisable(true);
        this.setEditable(false);
        this.setPickOnBounds(true);
        this.setFocusTraversable(false);
    }
}
