package cn.oyzh.fx.gui.label;

import javafx.scene.text.FontWeight;

/**
 * @author oyzh
 * @since 2024/04/08
 */
public class InfoLabel extends AccentLabel {

    public InfoLabel() {
        super();
    }

    public InfoLabel(String text) {
        super(text);
    }

    @Override
    public void initNode() {
        this.setRealHeight(30);
        super.disableFontWeight();
        this.setFontWeight(FontWeight.BOLD);
        super.initNode();
    }
}
