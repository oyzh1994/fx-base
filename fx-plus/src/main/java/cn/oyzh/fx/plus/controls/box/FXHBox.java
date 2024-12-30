package cn.oyzh.fx.plus.controls.box;

import cn.oyzh.fx.plus.adapter.LayoutAdapter;
import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeGroup;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.scene.Node;
import javafx.scene.layout.HBox;

/**
 * @author oyzh
 * @since 2022/1/19
 */
public class FXHBox extends HBox implements NodeGroup, ThemeAdapter, LayoutAdapter, FontAdapter, NodeAdapter, StateAdapter {

    {
        NodeManager.init(this);
    }

    public FXHBox() {
        super();
    }

    public FXHBox(Node... children) {
        super(children);
    }
}
