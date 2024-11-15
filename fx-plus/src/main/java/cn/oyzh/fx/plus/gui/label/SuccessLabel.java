package cn.oyzh.fx.plus.gui.label;

import cn.oyzh.fx.plus.controls.label.FlexLabel;
import javafx.scene.Node;

/**
 * @author oyzh
 * @since 2024/04/09
 */
public class SuccessLabel extends FlexLabel {

    {
        this.addClass("success");
    }

    public SuccessLabel() {
        super("");
    }

    public SuccessLabel(String text) {
        super(text);
    }

    public SuccessLabel(String text, Node graphic) {
        super(text, graphic);
    }
}
