package cn.oyzh.fx.plus.layout;


import cn.oyzh.fx.plus.adapter.LayoutAdapter;
import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeGroup;
import cn.oyzh.fx.plus.node.NodeUtil;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.beans.DefaultProperty;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;


/**
 * @author oyzh
 * @since 2025-02-07
 */
public class VBoxLayout extends Pane implements FlexAdapter, ThemeAdapter, FontAdapter, NodeGroup, LayoutAdapter, NodeAdapter, StateAdapter {

    private final static String MARGIN_CONSTRAINT = "vbox_layout_margin";

    public static Insets getMargin(Node child) {
        return (Insets) NodeUtil.getProperty(child, MARGIN_CONSTRAINT);
    }

    public static void setMargin(Node child, Insets insets) {
        NodeUtil.setProperty(child, MARGIN_CONSTRAINT, insets);
    }

    @Override
    public void resizeRelocate(double x, double y, double width, double height) {
        super.resizeRelocate(x, y, width, height);
    }

//    @Override
//    public Orientation getContentBias() {
//        return Orientation.VERTICAL;
//    }

    @Override
    public void resize(double width, double height) {
        double[] size = this.computeSize(width, height);
        super.resize(size[0], size[1]);
        this.resizeNode();
    }

    @Override
    protected void layoutChildren() {
        double y = 0;
        for (Node child : this.getChildren()) {
            child.autosize();
            Insets margin = getMargin(child);
            double areaX = margin == null ? 0 : margin.getLeft();
            double areaY = margin == null ? y : y + margin.getTop();
            child.setLayoutX(areaX);
            child.setLayoutY(areaY);
            y += NodeUtil.getHeight(child);
            if (margin != null) {
                y += margin.getTop() + margin.getBottom();
            }
        }
    }
}
