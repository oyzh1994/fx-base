package cn.oyzh.fx.gui.label;

import cn.oyzh.fx.plus.controls.label.FXLabel;
import javafx.scene.Node;
import javafx.scene.paint.Color;

/**
 * 提示标签，灰色文字
 *
 * @author oyzh
 * @since 2025-02-14
 */
public class TipsLabel extends FXLabel {

    /**
     * 构造提示标签对象。
     */
    public TipsLabel() {
        super("");
    }

    /**
     * 构造提示标签对象。
     *
     * @param text 文本
     */
    public TipsLabel(String text) {
        super(text);
    }

    /**
     * 构造提示标签对象。
     *
     * @param text 文本
     * @param graphic 图形
     */
    public TipsLabel(String text, Node graphic) {
        super(text, graphic);
    }

    @Override
    public void initNode() {
        super.initNode();
        this.setTextFill(Color.GRAY);
    }
}
