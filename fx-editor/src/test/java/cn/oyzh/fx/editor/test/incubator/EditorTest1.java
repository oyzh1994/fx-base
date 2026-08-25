package cn.oyzh.fx.editor.test.incubator;

import cn.oyzh.common.object.ObjectWatcherManager;
import cn.oyzh.fx.editor.incubator.Editor;
import cn.oyzh.fx.plus.node.NodeUtil;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.theme.Themes;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import jfx.incubator.scene.control.richtext.CodeArea;


/**
 * @author oyzh
 * @since 2022/5/18
 */
public class EditorTest1 extends Application {

    public static void main(String[] args) {
        launch(EditorTest1.class, args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        ThemeManager.apply(Themes.PRIMER_LIGHT);
        test1(stage);
        stage.getScene().getStylesheets().add("/fx-plus/css/fx-base.css");
        stage.setTitle("editor");
    }

    private void test1(Stage stage) {

        Button button = new Button("新页面");
        button.setOnAction(event -> {
            Stage stage1 = new Stage();
            Editor editor = new Editor();
            editor.appendText("hello editor");
            Scene scene1 = new Scene(editor);
            stage1.setScene(scene1);
            stage1.setWidth(800);
            stage1.setHeight(600);
            stage1.show();

            stage1.setOnHidden(event1 -> {
//                editor.getSkin().dispose();
                scene1.rootProperty().unbind();
//                editor.destroy();
                stage.setScene(null);
//                NodeUtil.removeNode(editor);
                System.out.println("close----");
            });
            ObjectWatcherManager.watch(stage1);
        });

        Scene scene = new Scene(button);
        stage.setScene(scene);
        stage.setWidth(300);
        stage.setHeight(200);
        stage.show();
    }

    public static class EditorTest1Starter {

        public static void main(String[] args) {
            EditorTest1.main(args);
        }

    }

}
