package cn.oyzh.fx.gui.label;

import cn.oyzh.fx.plus.controls.label.FXLabel;
import javafx.scene.Node;

/**
 * 强调色标签
 *
 * @author oyzh
 * @since 2024/04/08
 */
public class AccentLabel extends FXLabel {

    {
        this.addClass("accent");
    }

    /**
     * 构造强调色标签。
     */
    public AccentLabel() {
        super("");
    }

    /**
     * 以指定文本构造强调色标签。
     *
     * @param text 文本
     */
    public AccentLabel(String text) {
        super(text);
    }

    /**
     * 以指定文本和图标构造强调色标签。
     *
     * @param text    文本
     * @param graphic 图标
     */
    public AccentLabel(String text, Node graphic) {
        super(text, graphic);
    }
}
