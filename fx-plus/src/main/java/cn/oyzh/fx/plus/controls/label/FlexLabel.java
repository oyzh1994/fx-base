//package cn.oyzh.fx.plus.controls.label;
//
//import cn.oyzh.fx.plus.flex.FlexAdapter;
//import javafx.scene.Node;
//
///**
// * @author oyzh
// * @since 2023/1/13
// */
//public class FlexLabel extends FXLabel implements FlexAdapter {
//
//    public FlexLabel() {
//        super("");
//    }
//
//    public FlexLabel(String text) {
//        super(text);
//    }
//
//    public FlexLabel(String text, Node graphic) {
//        super(text, graphic);
//    }
//
//    @Override
//    public void resize(double width, double height) {
//        double[] size = this.computeSize(width, height);
//        super.resize(size[0], size[1]);
//        this.resizeNode();
//    }
//}
