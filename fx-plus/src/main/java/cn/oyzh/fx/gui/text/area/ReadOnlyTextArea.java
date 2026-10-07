package cn.oyzh.fx.gui.text.area;

import cn.oyzh.fx.plus.controls.text.area.FXTextArea;

/**
 * 只读文本域
 *
 * @author oyzh
 * @since 2022-01-20
 */
public class ReadOnlyTextArea extends FXTextArea {

    {
        this.setRequire(false);
        this.setEditable(false);
        this.setPickOnBounds(true);
        this.setFocusTraversable(false);
    }
}
