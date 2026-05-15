package cn.oyzh.fx.editor.test.incubator;

import cn.oyzh.common.object.ObjectWatcherManager;
import cn.oyzh.fx.editor.incubator.Editor;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.theme.Themes;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageExt;
import cn.oyzh.fx.plus.window.StageManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;


/**
 * @author oyzh
 * @since 2022/5/18
 */
public class EditorTest2 extends Application {

    public static void main(String[] args) {
        FXUtil.enablePreview();
        launch(EditorTest2.class, args);
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
            StageAdapter adapter =  StageManager.parseStage(Test1Controler.class);

            adapter.show();
        });

        Scene scene = new Scene(button);
        stage.setScene(scene);
        stage.setWidth(300);
        stage.setHeight(200);
        stage.show();
    }

    public static class EditorTest1Starter {

        public static void main(String[] args) {
            EditorTest2.main(args);
        }

    }

}
