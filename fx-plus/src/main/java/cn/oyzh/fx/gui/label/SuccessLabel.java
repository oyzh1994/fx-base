package cn.oyzh.fx.gui.label;

import cn.oyzh.fx.plus.controls.label.FXLabel;
import javafx.scene.Node;

/**
 * @author oyzh
 * @since 2024/04/09
 */
public class SuccessLabel extends FXLabel {

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
