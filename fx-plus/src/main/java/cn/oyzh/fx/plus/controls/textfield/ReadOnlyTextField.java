package cn.oyzh.fx.plus.controls.textfield;

/**
 * 只读文本控件
 *
 * @author oyzh
 * @since 2022/12/20
 */
public class ReadOnlyTextField extends FlexTextField {

    {
        this.setEditable(false);
        this.setPickOnBounds(true);
        this.setFocusTraversable(false);
    }
}
