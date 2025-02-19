package cn.oyzh.fx.plus.controls.label;

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
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.scene.Node;
import javafx.scene.control.Label;

/**
 * @author oyzh
 * @since 2020/10/29
 */
public class FXLabel extends Label implements FlexAdapter, NodeGroup, ThemeAdapter, MouseAdapter, TextAdapter, TipAdapter, StateAdapter, FontAdapter, LayoutAdapter, NodeAdapter {

    {
        NodeManager.init(this);
    }

    public FXLabel() {
        super("");
    }

    public FXLabel(String text) {
        super(text);
    }

    public FXLabel(String text, Node graphic) {
        super(text, graphic);
    }

    @Override
    public boolean isEnableTheme() {
        return ThemeAdapter.super.isEnableTheme();
    }

    @Override
    public void setEnableTheme(boolean enableTheme) {
        ThemeAdapter.super.setEnableTheme(enableTheme);
    }

    public void clear() {
        this.setText("");
    }

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
}
