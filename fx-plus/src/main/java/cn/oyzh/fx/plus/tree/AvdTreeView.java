// package cn.oyzh.fx.plus.tree;
//
// import cn.oyzh.fx.plus.controls.FXVBox;
// import cn.oyzh.fx.plus.controls.FlexHBox;
// import cn.oyzh.fx.plus.controls.FlexPane;
// import cn.oyzh.fx.plus.controls.FlexScrollPane;
// import cn.oyzh.fx.plus.controls.FlexVBox;
// import javafx.beans.value.ChangeListener;
// import javafx.beans.value.ObservableValue;
// import javafx.collections.ObservableList;
// import javafx.event.EventHandler;
// import javafx.geometry.Orientation;
// import javafx.scene.Node;
// import javafx.scene.control.ScrollBar;
// import javafx.scene.control.ScrollPane.ScrollBarPolicy;
// import javafx.scene.control.skin.ScrollPaneSkin;
// import javafx.scene.control.skin.TreeViewSkin;
// import javafx.scene.input.ScrollEvent;
// import lombok.Getter;
// import lombok.Setter;
//
// /**
//  * @author oyzh
//  * @since 2023/12/5
//  */
// public class AvdTreeView extends FlexPane {
//
//     @Getter
//     @Setter
//     private double fixedCellSize = 18;
//
//     {
//
//
//         ScrollBar s1 = new ScrollBar();
//
//         ScrollBar s2 = new ScrollBar();
//
//         s2.setOrientation(Orientation.VERTICAL);
//
//         FXVBox content = new FXVBox();
//         this.setContent(content);
// //        this.addChild(s1);
//         this.addChild(s2);
//
//         this.prefHeightProperty().addListener((observableValue, number, t1) -> {
// //            this.getContent().setPrefHeight(t1.doubleValue() - 50);
//             s2.setPrefHeight(t1.doubleValue());
//         });
//         this.minHeightProperty().addListener((observableValue, number, t1) -> {
// //            this.getContent().setMinHeight(t1.doubleValue() - 50);
//             s2.setMinHeight(t1.doubleValue());
//         });
//         this.maxHeightProperty().addListener((observableValue, number, t1) -> {
// //            this.getContent().setMaxHeight(t1.doubleValue() - 50);
//             s2.setMaxHeight(t1.doubleValue());
//         });
//         this.prefWidthProperty().addListener((observableValue, number, t1) -> {
//             this.getContent().setPrefWidth(t1.doubleValue() - 50);
//             s2.setLayoutX(this.getWidth() - 20);
//         });
//         this.minWidthProperty().addListener((observableValue, number, t1) -> {
//             this.getContent().setMinWidth(t1.doubleValue() - 50);
//             s2.setLayoutX(this.getWidth() - 20);
//         });
//         this.maxWidthProperty().addListener((observableValue, number, t1) -> {
//             this.getContent().setMaxWidth(t1.doubleValue() - 50);
//             s2.setLayoutX(this.getWidth() - 20);
//         });
//         s2.onScrollFinishedProperty().addListener((observableValue, eventHandler, t1) -> {
//             System.out.println(t1);
//         });
//         s2.valueProperty().addListener((observableValue, number, t1) -> {
//             System.out.println(t1);
//             double v1 = content.getHeight() * t1.doubleValue() / 100;
//             content.setLayoutY(-v1);
//         });
//
//     }
//
//     protected void setContent(FXVBox content) {
//         content.setId("content");
//         this.addChild(content);
//     }
//
//     public FXVBox getContent() {
//         return (FXVBox) this.lookup("#content");
//     }
//
//     public void setRoot(AvdTreeItem<?> item) {
//         this.getContent().setChild(item);
//         if (item != null) {
//             item.setRealHeight(this.fixedCellSize);
//         }
//     }
//
//     public AvdTreeItem<?> getRoot() {
//         return (AvdTreeItem<?>) this.getContent().getChild(0);
//     }
//
//     @Override
//     public void layoutChildren() {
//         super.layoutChildren();
//     }
//
//     @Override
//     public ObservableList<Node> getChildren() {
//         return super.getChildren();
//     }
// }
