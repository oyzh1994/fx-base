package cn.oyzh.fx.gui.label;

import cn.oyzh.fx.plus.controls.label.FlexLabel;
import cn.oyzh.fx.plus.theme.ThemeManager;
import javafx.scene.Node;
import javafx.scene.text.FontWeight;

/**
 * @author oyzh
 * @since 2024/04/08
 */
public class SystemLabel extends FlexLabel {

    public SystemLabel() {
        super("");
    }

    public SystemLabel(String text) {
        super(text);
    }

    public SystemLabel(String text, Node graphic) {
        super(text, graphic);
    }

    @Override
    public void initNode() {
        super.initNode();
        this.setTextFill(ThemeManager.currentAccentColor());
    }
}
