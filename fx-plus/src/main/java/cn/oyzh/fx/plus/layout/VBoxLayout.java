package cn.oyzh.fx.plus.layout;


import cn.oyzh.fx.plus.adapter.LayoutAdapter;
import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeGroup;
import cn.oyzh.fx.plus.node.NodeUtil;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;


/**
 * @author oyzh
 * @since 2025-02-07
 */
public class VBoxLayout extends BoxLayout implements FlexAdapter, ThemeAdapter, FontAdapter, NodeGroup, LayoutAdapter, NodeAdapter, StateAdapter {

    private final static String MARGIN_CONSTRAINT = "vbox_layout_margin";

    public static Insets getMargin(Node child) {
        return (Insets) NodeUtil.getProperty(child, MARGIN_CONSTRAINT);
    }

    public static void setMargin(Node child, Insets insets) {
        NodeUtil.setProperty(child, MARGIN_CONSTRAINT, insets);
    }

    public VBoxLayout() {
        super();
    }

    public VBoxLayout(Node... children) {
        super(children);
    }

    @Override
    public Orientation getContentBias() {
        return Orientation.VERTICAL;
    }

    @Override
    public void resize(double width, double height) {
        double[] size = this.computeSize(width, height);
        super.resize(size[0], size[1]);
        this.resizeNode();
    }

    @Override
    protected void layoutChildren() {
        Insets padding = this.getPadding();
        double y = padding == null ? 0 : padding.getTop();
        for (Node child : this.getManagedChildren()) {
            child.autosize();
            Insets margin = getMargin(child);
            if (margin == null) {
                child.setLayoutX(0);
                child.setLayoutY(y);
            } else {
                child.setLayoutX(margin.getLeft());
                child.setLayoutY(y + margin.getTop());
                y += margin.getTop() + margin.getBottom();
            }
            y += NodeUtil.getHeight(child);
            Insets padding1 = this.getPadding();
            if (padding1 != null) {
                y += padding1.getTop() + padding1.getBottom();
            }
            System.out.println(child.getBoundsInLocal());
            System.out.println(child.getBoundsInParent());
            System.out.println(child.getLayoutBounds());
            System.out.println("child: " + child);
        }
    }
}
