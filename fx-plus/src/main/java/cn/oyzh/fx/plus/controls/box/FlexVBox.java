//package cn.oyzh.fx.plus.controls.box;
//
//import cn.oyzh.fx.plus.flex.FlexAdapter;
//import javafx.scene.Node;
//
///**
// * @author oyzh
// * @since 2022/06/03
// */
//public class FlexVBox extends FXVBox implements FlexAdapter {
//
//    public FlexVBox() {
//        super();
//    }
//
//    public FlexVBox(Node... children) {
//        super(children);
//    }
//
//    @Override
//    public void resize(double width, double height) {
//        double[] size = this.computeSize(width, height);
//        super.resize(size[0], size[1]);
//        this.resizeNode();
//    }
//}
