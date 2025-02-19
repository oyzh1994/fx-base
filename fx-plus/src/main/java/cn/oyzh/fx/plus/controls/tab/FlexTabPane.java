//package cn.oyzh.fx.plus.controls.tab;
//
//import cn.oyzh.fx.plus.flex.FlexAdapter;
//import cn.oyzh.fx.plus.flex.FlexUtil;
//import cn.oyzh.fx.plus.node.NodeUtil;
//import javafx.scene.control.Tab;
//
///**
// * @author oyzh
// * @since 2022/1/20
// */
//public class FlexTabPane extends FXTabPane implements FlexAdapter {
//
//    @Override
//    public void resize(double width, double height) {
//        double[] size = this.computeSize(width, height);
//        super.resize(size[0], size[1]);
//        this.resizeNode();
//    }
//
//    @Override
//    public void resizeNode(Double width, Double height) {
//        FlexAdapter.super.resizeNode(width, height);
//        for (Tab tab : this.getTabs()) {
//            if (tab.getContent() instanceof FlexAdapter flexNode) {
//                flexNode.setRealWidth(FlexUtil.compute(flexNode.getFlexWidth(), width));
//                flexNode.setRealHeight(FlexUtil.compute(flexNode.getFlexHeight(), height));
//            } else {
//                NodeUtil.setWidth(tab.getContent(), width);
//                NodeUtil.setHeight(tab.getContent(), height);
//            }
//        }
//    }
//}
