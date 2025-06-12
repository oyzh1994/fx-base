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
    //     return this.getMinHeight();
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
    //     return this.getPrefHeight();
    // }

    @Override
    protected void layoutChildren() {
        // for (Node child : this.getChildren()) {
        //     child.autosize();
        // }
        super.layoutChildren();
        // List<Node> managed = getManagedChildren();
        // Insets insets = getInsets();
        // double width = getWidth();
        // double height = getHeight();
        // double top = snapSpaceY(insets.getTop());
        // double left = snapSpaceX(insets.getLeft());
        // double bottom = snapSpaceY(insets.getBottom());
        // double right = snapSpaceX(insets.getRight());
        // double space = snapSpaceY(getSpacing());
        // HPos hpos = getAlignmentInternal().getHpos();
        // VPos vpos = getAlignmentInternal().getVpos();
        // boolean isFillWidth = isFillWidth();
        //
        // double[][] actualAreaHeights = getAreaHeights(managed, width);
        // double contentWidth = width - left - right;
        // double contentHeight = this.realHeight();
        //
        // double x = left;
        // double y = top + computeYOffset(height - top - bottom, contentHeight, vpos);
        //
        // for (int i = 0, size = managed.size(); i < size; i++) {
        //     Node child = managed.get(i);
        //     layoutInArea(child, x, y, contentWidth, actualAreaHeights[0][i],
        //             /* baseline shouldn't matter */actualAreaHeights[0][i],
        //             getMargin(child), isFillWidth, true,
        //             hpos, vpos);
        //     y += actualAreaHeights[0][i] + space;
        // }
    }

    // private Pos getAlignmentInternal() {
    //     Pos localPos = getAlignment();
    //     return localPos == null ? Pos.TOP_LEFT : localPos;
    // }
    //
    // static double computeYOffset(double height, double contentHeight, VPos vpos) {
    //     switch (vpos) {
    //         case BASELINE:
    //         case TOP:
    //             return 0;
    //         case CENTER:
    //             return (height - contentHeight) / 2;
    //         case BOTTOM:
    //             return height - contentHeight;
    //         default:
    //             throw new AssertionError("Unhandled vPos");
    //     }
    // }
    //
    // private double[][] tempArray;
    //
    // private double[][] getTempArray(int size) {
    //     if (tempArray == null) {
    //         tempArray = new double[2][size]; // First array for the result, second for temporary computations
    //     } else if (tempArray[0].length < size) {
    //         tempArray = new double[2][Math.max(tempArray.length * 3, size)];
    //     }
    //     return tempArray;
    // }
    //
    // private double[][] getAreaHeights(List<Node> managed, double width) {
    //     // width could be -1
    //     double[][] temp = getTempArray(managed.size());
    //     final double insideWidth = width == -1 ? -1 : width -
    //             snapSpaceX(getInsets().getLeft()) - snapSpaceX(getInsets().getRight());
    //     final boolean isFillWidth = isFillWidth();
    //     for (int i = 0, size = managed.size(); i < size; i++) {
    //         Node child = managed.get(i);
    //         Insets margin = getMargin(child);
    //         if (insideWidth != -1 && isFillWidth) {
    //             temp[0][i] = margin.getTop() + child.prefWidth(-1) + margin.getBottom() + insideWidth;
    //         } else {
    //             temp[0][i] = margin.getTop() + child.prefWidth(-1) + margin.getBottom();
    //         }
    //     }
    //     return temp;
    // }
}
