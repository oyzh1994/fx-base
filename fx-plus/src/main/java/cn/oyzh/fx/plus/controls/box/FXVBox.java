package cn.oyzh.fx.plus.controls.box;

import cn.oyzh.fx.plus.adapter.LayoutAdapter;
import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeGroup;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

/**
 * @author oyzh
 * @since 2022/06/03
 */
public class FXVBox extends VBox implements FlexAdapter, NodeGroup, ThemeAdapter, FontAdapter, StateAdapter, NodeAdapter, LayoutAdapter {

    {
        NodeManager.init(this);
    }

    public FXVBox() {
        super();
    }

    public FXVBox(Node... children) {
        super(children);
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
