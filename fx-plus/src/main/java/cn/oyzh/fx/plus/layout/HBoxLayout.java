//package cn.oyzh.fx.plus.layout;
//
//
//import cn.oyzh.fx.plus.adapter.LayoutAdapter;
//import cn.oyzh.fx.plus.adapter.StateAdapter;
//import cn.oyzh.fx.plus.flex.FlexAdapter;
//import cn.oyzh.fx.plus.font.FontAdapter;
//import cn.oyzh.fx.plus.node.NodeAdapter;
//import cn.oyzh.fx.plus.node.NodeGroup;
//import cn.oyzh.fx.plus.node.NodeUtil;
//import cn.oyzh.fx.plus.theme.ThemeAdapter;
//import javafx.geometry.HPos;
//import javafx.geometry.Insets;
//import javafx.geometry.Orientation;
//import javafx.geometry.VPos;
//import javafx.scene.Node;
//import javafx.scene.layout.HBox;
//import javafx.scene.layout.Pane;
//import lombok.extern.slf4j.Slf4j;
//
//
///**
// * @author oyzh
// * @since 2025-02-07
// */
//public class HBoxLayout extends BoxLayout implements FlexAdapter, ThemeAdapter, FontAdapter, NodeGroup, LayoutAdapter, NodeAdapter, StateAdapter {
//
//    private final static String MARGIN_CONSTRAINT = "hbox_layout_margin";
//
//    public static Insets getMargin(Node child) {
//        return (Insets) NodeUtil.getProperty(child, MARGIN_CONSTRAINT);
//    }
//
//    public static void setMargin(Node child, Insets insets) {
//        NodeUtil.setProperty(child, MARGIN_CONSTRAINT, insets);
//    }
//
//    public HBoxLayout() {
//        super();
//    }
//
//    public HBoxLayout(Node... children) {
//        super(children);
//    }
//
//    @Override
//    public void resize(double width, double height) {
//        double[] size = this.computeSize(width, height);
//        super.resize(size[0], size[1]);
//        this.resizeNode();
//    }
//
//    @Override
//    public Orientation getContentBias() {
//        return Orientation.HORIZONTAL;
//    }
//
//    @Override
//    protected void layoutChildren() {
//        Insets padding = this.getPadding();
//        double x = padding == null ? 0 : padding.getLeft();
//        for (Node child : this.getManagedChildren()) {
//            child.autosize();
//            Insets margin = getMargin(child);
//            if (margin == null) {
//                child.setLayoutX(x);
//                child.setLayoutY(0);
//            } else {
//                child.setLayoutX(x + margin.getLeft());
//                child.setLayoutY(margin.getTop());
//                x += margin.getLeft() + margin.getRight();
//            }
//
//            x += NodeUtil.getWidth(child) + child.getLayoutBounds().getMinX();
//            Insets padding1 = this.getPadding();
//            if (padding1 != null) {
//                x += padding1.getLeft() + padding1.getRight();
//            }
//        }
//    }
//}
