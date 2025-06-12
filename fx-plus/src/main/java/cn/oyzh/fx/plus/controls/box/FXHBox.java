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
import javafx.scene.layout.HBox;

/**
 * @author oyzh
 * @since 2022/1/19
 */
public class FXHBox extends HBox implements FlexAdapter, NodeGroup, ThemeAdapter, LayoutAdapter, FontAdapter, NodeAdapter, StateAdapter {

    {
        NodeManager.init(this);
    }

    public FXHBox() {
        super();
    }

    public FXHBox(Node... children) {
        super(children);
    }

    @Override
    public void resize(double width, double height) {
        double[] size = this.computeSize(width, height);
        super.resize(size[0], size[1]);
        this.resizeNode();
    }

    // @Override
    // protected double computeMinWidth(double var1) {
    //     // Insets insets = getInsets();
    //     // double minWidth = this.getMinWidth();
    //     // return snapSpaceX(insets.getLeft()) + minWidth + snapSpaceX(insets.getRight());
    //     return this.getMinWidth();
    // }
    //
    // @Override
    // protected double computeMinHeight(double var1) {
    //     // Insets insets = getInsets();
    //     // double minHeight = this.getMinHeight();
    //     // return snapSpaceY(insets.getTop()) + minHeight + snapSpaceY(insets.getBottom());
    //     return this.realHeight();
    // }
    //
    // @Override
    // protected double computePrefWidth(double var1) {
    //     // Insets insets = getInsets();
    //     // double prefWidth = this.getPrefWidth();
    //     // return snapSpaceX(insets.getLeft()) + prefWidth + snapSpaceX(insets.getRight());
    //     return this.getPrefWidth();
    // }
    //
    // @Override
    // protected double computePrefHeight(double var1) {
    //     // Insets insets = getInsets();
    //     // double prefHeight = this.getPrefHeight();
    //     // return snapSpaceY(insets.getTop()) + prefHeight + snapSpaceY(insets.getBottom());
    //     return this.realHeight();
    // }
}
