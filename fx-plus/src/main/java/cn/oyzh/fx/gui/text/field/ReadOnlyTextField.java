package cn.oyzh.fx.gui.text.field;

import cn.oyzh.fx.plus.controls.text.field.FXTextField;

/**
 * 只读文本控件
 *
 * @author oyzh
 * @since 2023-10-09
 */
public class ReadOnlyTextField extends FXTextField {

    @Override
    public void initNode() {
        this.setEditable(false);
        this.setPickOnBounds(true);
        this.setFocusTraversable(false);
        super.initNode();
    }
}
