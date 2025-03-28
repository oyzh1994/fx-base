//package cn.oyzh.fx.plus.layout;
//
//import javafx.geometry.Orientation;
//import javafx.scene.Node;
//import javafx.scene.layout.Pane;
//
///**
// * @author oyzh
// * @since 2025-02-07
// */
//public class BoxLayout extends Pane {
//
//    public BoxLayout() {
//        super();
//    }
//
//    public BoxLayout(Node...children) {
//        super(children);
//    }
//
//    protected double[] boundedSize(Node node) {
//        Orientation contentBias = node.getContentBias();
//        double w, h;
//        if (contentBias == null) {
//            w = boundedSize(node.prefWidth(-1), node.minWidth(-1), node.maxWidth(-1));
//            h = boundedSize(node.prefHeight(-1), node.minHeight(-1), node.maxHeight(-1));
//        } else if (contentBias == Orientation.HORIZONTAL) {
//            w = boundedSize(node.prefWidth(-1), node.minWidth(-1), node.maxWidth(-1));
//            h = boundedSize(node.prefHeight(w), node.minHeight(w), node.maxHeight(w));
//        } else { // bias == VERTICAL
//            h = boundedSize(node.prefHeight(-1), node.minHeight(-1), node.maxHeight(-1));
//            w = boundedSize(node.prefWidth(h), node.minWidth(h), node.maxWidth(h));
//        }
//        return new double[]{w, h};
//    }
//
//    protected double boundedWidth(Node node) {
//        Orientation contentBias = node.getContentBias();
//        double w;
//        if (contentBias == null) {
//            w = boundedSize(node.prefWidth(-1), node.minWidth(-1), node.maxWidth(-1));
//        } else if (contentBias == Orientation.HORIZONTAL) {
//            w = boundedSize(node.prefWidth(-1), node.minWidth(-1), node.maxWidth(-1));
//        } else { // bias == VERTICAL
//           double h = boundedSize(node.prefHeight(-1), node.minHeight(-1), node.maxHeight(-1));
//            w = boundedSize(node.prefWidth(h), node.minWidth(h), node.maxWidth(h));
//        }
//        return w;
//    }
//
//    protected double boundedHeight(Node node) {
//        Orientation contentBias = node.getContentBias();
//        double  h;
//        if (contentBias == null) {
//            h = boundedSize(node.prefHeight(-1), node.minHeight(-1), node.maxHeight(-1));
//        } else if (contentBias == Orientation.HORIZONTAL) {
//           double w = boundedSize(node.prefWidth(-1), node.minWidth(-1), node.maxWidth(-1));
//            h = boundedSize(node.prefHeight(w), node.minHeight(w), node.maxHeight(w));
//        } else { // bias == VERTICAL
//            h = boundedSize(node.prefHeight(-1), node.minHeight(-1), node.maxHeight(-1));
//        }
//        return h;
//    }
//
//    protected double boundedSize(double value, double min, double max) {
//        // if max < value, return max
//        // if min > value, return min
//        // if min > max, return min
//        return Math.min(Math.max(value, min), Math.max(min, max));
//    }
//}
