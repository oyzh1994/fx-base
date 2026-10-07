package cn.oyzh.fx.gui.label;

import cn.oyzh.fx.plus.controls.label.FXLabel;
import javafx.scene.Node;

/**
 * 成功标签
 *
 * @author oyzh
 * @since 2024/04/09
 */
public class SuccessLabel extends FXLabel {

    {
        this.addClass("success");
    }

    /**
     * 构造成功标签对象。
     */
    public SuccessLabel() {
        super("");
    }

    /**
     * 构造成功标签对象。
     *
     * @param text 文本
     */
    public SuccessLabel(String text) {
        super(text);
    }

    /**
     * 构造成功标签对象。
     *
     * @param text 文本
     * @param graphic 图形
     */
    public SuccessLabel(String text, Node graphic) {
        super(text, graphic);
    }
}
