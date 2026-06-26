package cn.oyzh.fx.plus.test.memory;

import cn.oyzh.common.object.ObjectWatcherManager;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.theme.Themes;
import cn.oyzh.fx.plus.window.PopupAdapter;
import cn.oyzh.fx.plus.window.PopupExt;
import cn.oyzh.fx.plus.window.PopupManager;
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;


/**
 * @author oyzh
 * @since 2022/5/18
 */
public class PopupExt1Test extends Application {

    public static void main(String[] args) {
        launch(PopupExt1Test.class, args);
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
            PopupAdapter popup= PopupManager.parsePopup(Test1PopupController.class);
            popup.show(button);
            ObjectWatcherManager.watch(popup);
        });

        Scene scene = new Scene(button);
        stage.setScene(scene);
        stage.setWidth(300);
        stage.setHeight(200);
        stage.show();
    }

    public static class PopupExt1TestStarter {

        public static void main(String[] args) {
            PopupExt1Test.main(args);
        }

    }

}
