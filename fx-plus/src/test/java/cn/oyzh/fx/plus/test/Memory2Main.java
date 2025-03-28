// package cn.oyzh.fx.plus.test;
//
// import cn.oyzh.common.file.FileUtil;
// import cn.oyzh.common.system.SystemUtil;
// import cn.oyzh.fx.plus.controls.box.FlexHBox;
// import cn.oyzh.fx.plus.controls.box.FlexVBox;
// import cn.oyzh.fx.plus.gui.button.AccentButton;
// import cn.oyzh.fx.plus.gui.button.AddConnectButton;
// import cn.oyzh.fx.plus.gui.button.ClearButton;
// import cn.oyzh.fx.plus.gui.button.ImportButton;
// import cn.oyzh.fx.plus.gui.button.NextButton;
// import cn.oyzh.fx.plus.gui.button.OpenTerminalButton;
// import cn.oyzh.fx.plus.gui.button.RunSqlFileButton;
// import cn.oyzh.fx.plus.gui.button.SaveButton;
// import cn.oyzh.fx.plus.gui.button.StartButton;
// import cn.oyzh.fx.plus.gui.button.StopButton;
// import cn.oyzh.fx.plus.gui.button.SubmitButton;
// import cn.oyzh.fx.plus.gui.button.SuccessButton;
// import cn.oyzh.fx.plus.gui.button.TestButton;
// import cn.oyzh.fx.plus.gui.button.TransportButton;
// import cn.oyzh.fx.plus.gui.button.UnLockButton;
// import cn.oyzh.fx.plus.gui.svg.glyph.AddSVGGlyph;
// import cn.oyzh.fx.plus.gui.svg.glyph.ChooseSVGGlyph;
// import cn.oyzh.fx.plus.gui.svg.glyph.CloseSVGGlyph;
// import cn.oyzh.fx.plus.controls.tab.FXTab;
// import cn.oyzh.fx.plus.controls.tab.FlexTabPane;
// import cn.oyzh.fx.plus.controls.tree.FlexTreeView;
// import javafx.application.Application;
// import javafx.scene.Scene;
// import javafx.scene.control.Button;
// import javafx.scene.control.TreeItem;
// import javafx.scene.paint.Color;
// import javafx.stage.Stage;
//
// import java.util.List;
//
//
// /**
//  * @author oyzh
//  * @since 2022/5/18
//  */
// public class Memory2Main extends Application {
//
//     private final long start = System.currentTimeMillis();
//
//     public static void main(String[] args) {
//         launch(Memory2Main.class, args);
//     }
//
//     @Override
//     public void start(Stage stage)   {
//         stage.setTitle("内存测试2");
//         test2(stage);
//         long end = System.currentTimeMillis();
//         double usedMemory = SystemUtil.getUsedMemory();
//         System.err.println("启动耗时" + (end - start) + "ms-------------------------------");
//         System.err.println("内存消耗" + (usedMemory) + "mb-------------------------------");
//
//         String str1 = "启动耗时" + (end - start) + "ms";
//         String str2 = "内存消耗" + usedMemory + "mb";
//         String str3 = "--------------------------------------------->";
//         FileUtil.appendLines(List.of(str1, str2, str3), "d://memory2.txt", "utf-8");
//     }
//
//     private void test2(Stage stage) {
//         FlexTreeView treeView = new FlexTreeView();
//         TreeItem<String> root = new TreeItem<>("11");
//         ChooseSVGGlyph glyph = new ChooseSVGGlyph();
//         glyph.setColor(Color.BLACK);
//         root.setGraphic(glyph);
//         treeView.setRoot(root);
//
//         treeView.setPrefHeight(200);
//
//         FlexVBox vBox = new FlexVBox(treeView);
//
//         stage.setScene(new Scene(vBox, 800, 800));
//         stage.show();
//
//         for (int i = 0; i < 300; i++) {
//             TreeItem<String> treeItem = new TreeItem<>("22");
//             AddSVGGlyph glyph1 = new AddSVGGlyph();
//             treeItem.setGraphic(glyph1);
//             root.getChildren().add(treeItem);
//         }
//
//         FlexHBox hBox = new FlexHBox();
//         hBox.getChildren().add(new Button("test1"));
//         hBox.getChildren().add(new Button("test2"));
//         hBox.getChildren().add(new Button("test3"));
//         hBox.getChildren().add(new AddConnectButton());
//         hBox.getChildren().add(new AddSVGGlyph());
//         hBox.getChildren().add(new CloseSVGGlyph());
//
//         vBox.getChildren().add(hBox);
//
//         FlexHBox hBox2 = new FlexHBox();
//         hBox2.getChildren().add(new StopButton());
//         hBox2.getChildren().add(new AddConnectButton());
//         hBox2.getChildren().add(new SubmitButton());
//         hBox2.getChildren().add(new AddConnectButton());
//         hBox2.getChildren().add(new AddSVGGlyph());
//         hBox2.getChildren().add(new CloseSVGGlyph());
//         vBox.getChildren().add(hBox2);
//
//         FlexHBox hBox3 = new FlexHBox();
//         hBox3.getChildren().add(new SaveButton());
//         hBox3.getChildren().add(new StartButton());
//         hBox3.getChildren().add(new SuccessButton());
//         hBox3.getChildren().add(new TestButton());
//         hBox3.getChildren().add(new TransportButton());
//         hBox3.getChildren().add(new UnLockButton());
//         vBox.getChildren().add(hBox3);
//
//         FlexHBox hBox4 = new FlexHBox();
//         hBox4.getChildren().add(new RunSqlFileButton());
//         hBox4.getChildren().add(new OpenTerminalButton());
//         hBox4.getChildren().add(new NextButton());
//         hBox4.getChildren().add(new ImportButton());
//         hBox4.getChildren().add(new AccentButton());
//         hBox4.getChildren().add(new ClearButton());
//         vBox.getChildren().add(hBox4);
//
//         FlexTabPane tabPane = new FlexTabPane();
//         tabPane.setPrefHeight(300);
//
//         for (int j = 0; j < 5; j++) {
//             FXTab tab1 = new FXTab("tab" + j);
//             FlexTreeView treeView1 = new FlexTreeView();
//             TreeItem<String> root1 = new TreeItem<>("11");
//             ChooseSVGGlyph glyph1 = new ChooseSVGGlyph();
//             glyph1.setColor(Color.BLACK);
//             root.setGraphic(glyph1);
//             treeView1.setRoot(root1);
//             treeView1.setPrefHeight(200);
//
//             for (int i = 0; i < 300; i++) {
//                 TreeItem<String> treeItem = new TreeItem<>("22");
//                 AddSVGGlyph glyph2 = new AddSVGGlyph();
//                 treeItem.setGraphic(glyph2);
//                 root1.getChildren().add(treeItem);
//             }
//
//             tab1.setContent(treeView1);
//             tabPane.getTabs().add(tab1);
//         }
//
//         vBox.getChildren().add(tabPane);
//     }
//
// }
