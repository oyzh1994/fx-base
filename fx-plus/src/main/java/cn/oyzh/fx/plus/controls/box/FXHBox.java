package cn.oyzh.fx.plus.controls.box;

import cn.oyzh.fx.plus.adapter.LayoutAdapter;
import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.node.NodeGroup;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.scene.Node;
import javafx.scene.layout.HBox;

/**
 * 水平布局容器，继承自 HBox，支持主题、字体、状态、布局等适配
 *
 * @author oyzh
 * @since 2023-10-09
 */
public class FXHBox extends HBox implements FlexAdapter, NodeGroup, ThemeAdapter, LayoutAdapter, FontAdapter, StateAdapter {

    {
        NodeManager.init(this);
    }

    /**
     * 构造面板对象。
     */
    public FXHBox() {
        super();
    }

    /**
     * 构造面板对象。
     *
     * @param children 子节点集合
     */
    public FXHBox(Node... children) {
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
