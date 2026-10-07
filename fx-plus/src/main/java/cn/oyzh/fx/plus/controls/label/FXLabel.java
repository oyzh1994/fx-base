package cn.oyzh.fx.plus.controls.label;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.adapter.LayoutAdapter;
import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.adapter.TextAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.mouse.MouseAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeGroup;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.scene.Node;
import javafx.scene.control.Label;

/**
 * 标签控件
 *
 * @author oyzh
 * @since 2023-10-09
 */
public class FXLabel extends Label implements FlexAdapter, NodeGroup, ThemeAdapter, MouseAdapter, TextAdapter, TipAdapter, StateAdapter, FontAdapter, LayoutAdapter, NodeAdapter {

    {
        NodeManager.init(this);
    }

    /**
     * 构造标签对象。
     */
    public FXLabel() {
        super("");
    }

    /**
     * 构造标签对象。
     *
     * @param graphic 图形
     */
    public FXLabel(Node graphic) {
        super("", graphic);
    }

    /**
     * 构造标签对象。
     *
     * @param text 文本
     */
    public FXLabel(String text) {
        super(text);
    }

    /**
     * 构造标签对象。
     *
     * @param text 文本
     * @param graphic 图形
     */
    public FXLabel(String text, Node graphic) {
        super(text, graphic);
    }

    /**
     * 文本是否为空
     *
     * @return 是否为空
     */
    public boolean isEmpty() {
        return StringUtil.isEmpty(this.getText());
    }

    /**
     * 清空文本
     */
    public void clear() {
        this.text("");
    }

    /**
     * 设置文本内容（在 JavaFX 线程中执行）
     *
     * @param text 文本内容
     */
    public void text(String text) {
        if (text != null) {
            FXUtil.runWait(() -> super.setText(text));
        }
    }

    @Override
    public void resize(double width, double height) {
        double[] size = this.computeSize(width, height);
        super.resize(size[0], size[1]);
        this.resizeNode();
    }

    @Override
    public void changeTheme(ThemeStyle style) {
        ThemeAdapter.super.changeTheme(style);
        if (this.getGraphic() instanceof ThemeAdapter adapter) {
            adapter.changeTheme(style);
        }
    }
}
