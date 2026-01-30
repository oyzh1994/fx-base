package cn.oyzh.fx.gui.label;

import cn.oyzh.fx.plus.controls.label.FXLabel;
import javafx.scene.Node;
import javafx.scene.paint.Color;

/**
 * @author oyzh
 * @since 2024/04/08
 */
public class TipsLabel extends FXLabel {

    public TipsLabel() {
        super("");
    }

    public TipsLabel(String text) {
        super(text);
    }

    public TipsLabel(String text, Node graphic) {
        super(text, graphic);
    }

    @Override
    public void initNode() {
        super.initNode();
        this.setTextFill(Color.GRAY);
    }
}
