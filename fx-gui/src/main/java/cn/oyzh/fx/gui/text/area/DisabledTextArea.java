package cn.oyzh.fx.gui.text.area;

import cn.oyzh.fx.plus.controls.text.area.FlexTextArea;

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
