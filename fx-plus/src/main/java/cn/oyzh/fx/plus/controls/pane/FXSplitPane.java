package cn.oyzh.fx.plus.controls.pane;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.flex.FlexUtil;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeGroup;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;

/**
 *
 * @author oyzh
 * @since 2025-11-27
 */
public class FXSplitPane extends SplitPane implements FlexAdapter, NodeGroup, NodeAdapter, TipAdapter, StateAdapter, FontAdapter, ThemeAdapter {

    {
        NodeManager.init(this);
    }

    @Override
    public void resize(double width, double height) {
        double[] size = this.computeSize(width, height);
        super.resize(size[0], size[1]);
        this.resizeNode();
    }

    @Override
    protected void layoutChildren() {
        for (Node child : this.getChildren()) {
            child.autosize();
        }
        super.layoutChildren();
    }
}
