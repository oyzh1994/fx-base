package cn.oyzh.fx.plus.controls.box;

import cn.oyzh.fx.plus.adapter.LayoutAdapter;
import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.node.NodeGroup;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

/**
 * 垂直布局容器，继承自 VBox，支持主题、字体、状态、布局等适配
 *
 * @author oyzh
 * @since 2023-10-09
 */
public class FXVBox extends VBox implements FlexAdapter, NodeGroup, ThemeAdapter, FontAdapter, StateAdapter, LayoutAdapter {

    {
        NodeManager.init(this);
    }

    /**
     * 构造面板对象。
     */
    public FXVBox() {
        super();
    }

    /**
     * 构造面板对象。
     *
     * @param children 子节点集合
     */
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

//    @Override
//    public void initNode() {
//        this.setCache(false);
//        FlexAdapter.super.initNode();
//    }
}
