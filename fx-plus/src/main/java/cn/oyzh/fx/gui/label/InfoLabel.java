package cn.oyzh.fx.gui.label;

import javafx.scene.text.FontWeight;

/**
 * 信息标签，加粗显示
 *
 * @author oyzh
 * @since 2024/04/08
 */
public class InfoLabel extends AccentLabel {

    /**
     * 构造信息标签对象。
     */
    public InfoLabel() {
        super();
    }

    /**
     * 构造信息标签对象。
     *
     * @param text 文本
     */
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
