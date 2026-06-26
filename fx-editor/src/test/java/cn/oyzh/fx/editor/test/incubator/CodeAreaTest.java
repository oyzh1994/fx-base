package cn.oyzh.fx.editor.test.incubator;

import cn.oyzh.common.object.ObjectWatcherManager;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.theme.Themes;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import jfx.incubator.scene.control.richtext.CodeArea;
import jfx.incubator.scene.control.richtext.RichTextArea;


/**
 * @author oyzh
 * @since 2022/5/18
 */
public class CodeAreaTest extends Application {

    public static void main(String[] args) {
        launch(CodeAreaTest.class, args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        ObjectWatcherManager.enable();
        ThemeManager.apply(Themes.PRIMER_LIGHT);
        test1(stage);
        stage.getScene().getStylesheets().add("/fx-plus/css/fx-base.css");
        stage.setTitle("code area");
    }

    private void test1(Stage stage) {

        Button button = new Button("新页面");
        button.setOnAction(event -> {
            Stage stage1 = new Stage();
            CodeArea codeArea = new CodeArea();
            codeArea.appendText("hello code area");
            Scene scene1 = new Scene(codeArea);
            stage1.setScene(scene1);
            stage1.setWidth(800);
            stage1.setHeight(600);
            stage1.show();

            stage1.setOnHidden(event1 -> {
                codeArea.getSkin().dispose();
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

    public static class CodeAreaTestStarter {

        public static void main(String[] args) {
            CodeAreaTest.main(args);
        }

    }

}
