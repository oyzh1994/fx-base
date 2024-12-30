package cn.oyzh.fx.plus.controls.pane;

import cn.oyzh.fx.plus.adapter.LayoutAdapter;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.scene.layout.Pane;

/**
 * @author oyzh
 * @since 2022/1/18
 */
public class FXPane extends Pane implements LayoutAdapter, NodeAdapter, ThemeAdapter, FontAdapter {

    {
        NodeManager.init(this);
    }
}
