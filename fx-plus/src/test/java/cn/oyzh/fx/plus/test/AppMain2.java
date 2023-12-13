// package cn.oyzh.fx.plus.test;
//
// import cn.oyzh.fx.plus.controls.FlexImageView;
// import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
// import cn.oyzh.fx.plus.tree.AvdTreeItem;
// import cn.oyzh.fx.plus.tree.AvdTreeView;
// import javafx.application.Application;
// import javafx.scene.Node;
// import javafx.scene.Scene;
// import javafx.scene.layout.HBox;
// import javafx.stage.Stage;
//
// import java.util.Objects;
// import java.util.concurrent.atomic.AtomicBoolean;
//
//
// /**
//  * @author oyzh
//  * @since 2022/5/18
//  */
// public class AppMain2 extends Application {
//
//     public static void main(String[] args) {
//         launch(AppMain2.class, args);
//     }
//
//     @Override
//     public void start(Stage stage) throws Exception {
//         this.test1(stage);
//     }
//
//     private void test1(Stage stage) {
//         AvdTreeView treeView = new AvdTreeView();
//         // treeView.setFlexWidth("100%");
//         // treeView.setFlexHeight("100%");
//         treeView.setPrefHeight(300);
//         treeView.setPrefWidth(300);
//         AvdTreeItem<String> root = new AvdTreeItem<>(treeView, null, "root", new FlexImageView("/zoo.jpg", 18));
//         treeView.setRoot(root);
//         this.initExtend(root, treeView, 0);
//         HBox group = new HBox(treeView);
//         stage.setScene(new Scene(group, 500, 500));
//         stage.show();
//         System.out.println(root.getHeight());
//         System.out.println(root.getContent().getHeight());
//     }
//
//
//     private void initExtend(AvdTreeItem<?> root, AvdTreeView treeView, int deep) {
//         if (deep > 5) {
//             return;
//         }
//         AtomicBoolean first = new AtomicBoolean(true);
//         root.expandedProperty().addListener((observable, oldValue, newValue) -> {
//             if (newValue && first.get()) {
//                 for (int i = 0; i < 50; i++) {
//                     AvdTreeItem<String> sub1 = new AvdTreeItem<>(treeView, root, "sub" + i + "(deep=" + deep + ")", new SVGGlyph("/folder.svg", 14));
//                     root.getChildren().add(sub1);
//                     this.initExtend(sub1, treeView, deep + 1);
//                     System.out.println(i);
//                 }
//                 first.set(false);
//             }
//         });
//     }
//
//
// }
