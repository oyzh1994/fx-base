package cn.oyzh.fx.plus.controls.pane;

import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.flex.FlexUtil;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.node.NodeUtil;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.scene.control.ScrollPane;

/**
 * @author oyzh
 * @since 2023/12/6
 */
public class FXScrollPane extends ScrollPane implements FlexAdapter, ThemeAdapter {

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
    public void resizeNode(Double width, Double height) {
        FlexAdapter.super.resizeNode(width, height);
        if (this.getContent() instanceof FlexAdapter flexNode) {
            flexNode.setRealWidth(FlexUtil.compute(flexNode.getFlexWidth(), width));
            flexNode.setRealHeight(FlexUtil.compute(flexNode.getFlexHeight(), height));
        } else {
            NodeUtil.setWidth(this.getContent(), width);
            NodeUtil.setHeight(this.getContent(), height);
        }
    }
}
