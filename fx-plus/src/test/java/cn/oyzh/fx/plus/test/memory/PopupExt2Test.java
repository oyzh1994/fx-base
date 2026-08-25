package cn.oyzh.fx.plus.test.memory;

import cn.oyzh.common.object.ObjectWatcherManager;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.theme.Themes;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.window.PopupAdapter;
import cn.oyzh.fx.plus.window.PopupManager;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;


/**
 * @author oyzh
 * @since 2022/5/18
 */
public class PopupExt2Test extends Application {

    public static void main(String[] args) {
        FXUtil.enablePreview();
        launch(PopupExt2Test.class, args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        ThemeManager.apply(Themes.PRIMER_LIGHT);
        test1(stage);
        stage.getScene().getStylesheets().add("/fx-plus/css/fx-base.css");
        stage.setTitle("popup ext1");
    }

    private void test1(Stage stage) {
        ObjectWatcherManager.enable();
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

    public static class PopupExt2TestStarter {

        public static void main(String[] args) {
            PopupExt2Test.main(args);
        }

    }

}
