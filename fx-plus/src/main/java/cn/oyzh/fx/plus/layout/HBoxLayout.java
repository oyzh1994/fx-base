package cn.oyzh.fx.plus.layout;


import cn.oyzh.fx.plus.adapter.LayoutAdapter;
import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeGroup;
import cn.oyzh.fx.plus.node.NodeUtil;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.Pane;


/**
 * @author oyzh
 * @since 2025-02-07
 */
public class HBoxLayout extends Pane implements FlexAdapter, ThemeAdapter, FontAdapter, NodeGroup, LayoutAdapter, NodeAdapter, StateAdapter {

    private final static String MARGIN_CONSTRAINT = "hbox_layout_margin";

    public static Insets getMargin(Node child) {
        return (Insets) NodeUtil.getProperty(child, MARGIN_CONSTRAINT);
    }

    public static void setMargin(Node child, Insets insets) {
        NodeUtil.setProperty(child, MARGIN_CONSTRAINT, insets);
    }

    @Override
    public void resize(double width, double height) {
        double[] size = this.computeSize(width, height);
        super.resize(size[0], size[1]);
        this.resizeNode();
    }

    @Override
    protected void layoutChildren() {
        double x = 0;
        for (Node child : this.getChildren()) {
            child.autosize();
            Insets margin = getMargin(child);
            double areaX = margin == null ? x : x + margin.getLeft();
            double areaY = margin == null ? 0 : margin.getTop();
            child.setLayoutX(areaX);
            child.setLayoutY(areaY);
            x += NodeUtil.getWidth(child);
            if (margin != null) {
                x += margin.getLeft() + margin.getRight();
            }
        }
    }
}
