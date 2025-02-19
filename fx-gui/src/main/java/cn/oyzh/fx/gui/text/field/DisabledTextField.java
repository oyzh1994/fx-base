package cn.oyzh.fx.gui.text.field;

import cn.oyzh.fx.plus.controls.text.field.FXTextField;

/**
 * 禁用文本控件
 *
 * @author oyzh
 * @since 2020/10/29
 */
public class DisabledTextField extends FXTextField {

    {
        this.setDisable(true);
        this.setEditable(false);
        this.setPickOnBounds(true);
//        this.setFocusTraversable(false);
    }
}
