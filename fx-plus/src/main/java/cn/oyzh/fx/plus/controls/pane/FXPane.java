package cn.oyzh.fx.plus.controls.pane;

import cn.oyzh.fx.plus.adapter.LayoutAdapter;
import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

/**
 * @author oyzh
 * @since 2022/1/18
 */
public class FXPane extends Pane implements FlexAdapter, LayoutAdapter, NodeAdapter, ThemeAdapter, FontAdapter {

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
