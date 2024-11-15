package cn.oyzh.fx.plus.gui.label;

import cn.oyzh.fx.plus.controls.label.FlexLabel;
import javafx.scene.Node;

/**
 * @author oyzh
 * @since 2024/04/08
 */
public class AccentLabel extends FlexLabel {

    {
        this.addClass("accent");
    }

    public AccentLabel() {
        super("");
    }

    public AccentLabel(String text) {
        super(text);
    }

    public AccentLabel(String text, Node graphic) {
        super(text, graphic);
    }
}
