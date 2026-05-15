package cn.oyzh.fx.editor.test.incubator;

import cn.oyzh.common.object.ObjectWatcherManager;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.theme.Themes;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import jfx.incubator.scene.control.richtext.RichTextArea;


/**
 * @author oyzh
 * @since 2022/5/18
 */
public class RichTextAreaTest extends Application {

    public static void main(String[] args) {
        launch(RichTextAreaTest.class, args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        ThemeManager.apply(Themes.PRIMER_LIGHT);
        test1(stage);
        stage.getScene().getStylesheets().add("/fx-plus/css/fx-base.css");
        stage.setTitle("richtext area");
    }

    private void test1(Stage stage) {

        Button button = new Button("新页面");
        button.setOnAction(event -> {
            Stage stage1 = new Stage();
            RichTextArea textArea = new RichTextArea();
            textArea.appendText("hello richtext area");
            Scene scene1 = new Scene(textArea);
            stage1.setScene(scene1);
            stage1.setWidth(800);
            stage1.setHeight(600);
            stage1.show();

            stage1.setOnHidden(event1 -> {
//                textArea.setModel(null);
//                textArea.modelProperty().unbind();
                textArea.getSkin().dispose();
//                textArea.skinProperty().unbind();
//                textArea.setSkin(null);
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

    public static class RichTextAreaTestStarter {

        public static void main(String[] args) {
            RichTextAreaTest.main(args);
        }

    }

}
