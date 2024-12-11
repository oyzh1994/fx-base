package cn.oyzh.fx.gui.label;

import cn.oyzh.fx.plus.controls.label.FlexLabel;
import javafx.scene.Node;
import javafx.scene.text.FontWeight;

/**
 * @author oyzh
 * @since 2024/04/08
 */
public class InfoLabel extends AccentLabel {

    public InfoLabel() {
        this("");
    }

    public InfoLabel(String text) {
        super(text);
        this.setRealHeight(30);
        this.setFontWeight(FontWeight.BOLD);
    }
}
