package cn.oyzh.fx.plus.test.memory;

import cn.oyzh.common.object.ObjectWatcherManager;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.theme.Themes;
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Popup;
import javafx.stage.Stage;


/**
 * @author oyzh
 * @since 2022/5/18
 */
public class PopupTest extends Application {

    public static void main(String[] args) {
        launch(PopupTest.class, args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        ThemeManager.apply(Themes.PRIMER_LIGHT);
        test1(stage);
        stage.getScene().getStylesheets().add("/fx-plus/css/fx-base.css");
        stage.setTitle("popup");
    }

    private void test1(Stage stage) {
        ObjectWatcherManager.enable();
        Button button = new Button("新页面");
        button.setOnAction(event -> {
            Popup popup = new Popup();
            popup.setAutoHide(true);
            popup.getContent().setAll(new Label("测试"));
            Point2D point2D = button.localToScreen(button.getScaleX(), button.getScaleY());
            popup.show(button, point2D.getX(), point2D.getY());
            popup.setOnHidden(event1 -> {
                System.out.println("close----");
            });
            ObjectWatcherManager.watch(popup);
        });

        Scene scene = new Scene(button);
        stage.setScene(scene);
        stage.setWidth(300);
        stage.setHeight(200);
        stage.show();
    }

    public static class PopupTestStarter {

        public static void main(String[] args) {
            PopupTest.main(args);
        }

    }

}
