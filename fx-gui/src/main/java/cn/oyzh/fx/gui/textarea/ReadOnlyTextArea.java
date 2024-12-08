package cn.oyzh.fx.gui.textarea;

import cn.oyzh.fx.plus.controls.textarea.FlexTextArea;

/**
 * @author oyzh
 * @since 2022/1/20
 */
public class ReadOnlyTextArea extends FlexTextArea {

    {
        this.setRequire(false);
        this.setEditable(false);
        this.setPickOnBounds(true);
        this.setFocusTraversable(false);
    }
}
