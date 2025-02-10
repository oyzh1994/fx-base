package cn.oyzh.fx.gui.text.area;

import cn.oyzh.fx.plus.controls.text.area.FlexTextArea;

/**
 * @author oyzh
 * @since 2022/1/20
 */
public class ReadOnlyTextArea extends FlexTextArea {

    {
        this.setRequire(false);
        this.setEditable(false);
        this.setPickOnBounds(true);
//        this.setFocusTraversable(false);
    }
}
