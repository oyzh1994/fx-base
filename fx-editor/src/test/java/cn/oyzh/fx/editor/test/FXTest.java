// package cn.oyzh.fx.editor.test;
//
// import cn.oyzh.fx.plus.controls.pane.FXScrollPane;
// import javafx.application.Application;
// import javafx.embed.swing.SwingNode;
// import javafx.scene.Scene;
// import javafx.scene.layout.VBox;
// import javafx.stage.Stage;
// import org.fife.ui.rsyntaxtextarea.TextEditorPane;
// import org.fife.ui.rtextarea.RTextScrollPane;
//
// import javax.swing.SwingUtilities;
//
// /**
//  * @author oyzh
//  * @since 2025-08-04
//  */
// public class FXTest extends Application {
//
//     public static void main(String[] args) {
//         launch(args);
//     }
//
//     @Override
//     public void start(Stage stage) throws Exception {
//
//         TextEditorPane editor = new TextEditorPane();
//         RTextScrollPane swingScrollPane = new RTextScrollPane(editor);
//         swingScrollPane.setLineNumbersEnabled(true);
//
//         SwingNode swingNode = new SwingNode();
//         swingNode.setContent(swingScrollPane);
//
//         SwingUtilities.invokeAndWait(() -> {
//            editor.setText("Hello World");
//         });
//
//         // 3. 创建JavaFX原生ScrollPane，并将SwingNode作为内容
//         FXScrollPane fxScrollPane = new FXScrollPane();
//         fxScrollPane.setContent(swingNode);
//
//
//         VBox vBox = new VBox(swingNode);
//
//         Scene scene = new Scene(vBox);
//         stage.setScene(scene);
//         stage.setWidth(800);
//         stage.setHeight(600);
//         stage.show();
//     }
//
//     public static class FXTestStarter {
//         public static void main(String[] args) {
//             FXTest.main(args);
//         }
//     }
// }
