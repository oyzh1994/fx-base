//package cn.oyzh.fx.plus.rich;
//
//import javafx.scene.Node;
//import javafx.scene.layout.Pane;
//import javafx.scene.text.Text;
//import javafx.scene.text.TextFlow;
//
//public class NonWrappingTextFlow extends Pane {
//
//    public NonWrappingTextFlow(TextFlow textFlow) {
//        getChildren().add(textFlow);
//        this.setPrefWidth(textFlow.getWidth());
//    }
//
//    @Override
//    protected void layoutChildren() {
//        double x = 0;
//        double y = 0;
//        TextFlow textFlow = (TextFlow) getChildren().getFirst();
//        // 遍历 TextFlow 中的所有 Text 节点
//        for (Node child : textFlow.getChildren()) {
//            if (child instanceof Text text) {
//                // 手动设置每个 Text 节点的位置
//                text.setLayoutX(x);
//                text.setLayoutY(y);
//                // 更新下一个 Text 节点的起始 x 坐标
//                x += text.getLayoutBounds().getWidth();
//            }
//        }
//    }
//}
