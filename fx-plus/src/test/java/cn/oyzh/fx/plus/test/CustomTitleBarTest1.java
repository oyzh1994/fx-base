package cn.oyzh.fx.plus.test;

import cn.oyzh.fx.plus.controls.FXHeaderBar;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * @author oyzh
 * @since 2025-08-18
 */
public class CustomTitleBarTest1 extends Application {

    public static void main(String[] args) {
        System.setProperty("javafx.enablePreview", "true");
        System.setProperty("javafx.suppressPreviewWarning", "true");
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXHeaderBar headerBar = new FXHeaderBar();
        Button button = new Button("test");
        button.setOnAction(event -> {
            if (stage.isFullScreen()) {
                stage.setFullScreen(false);
            } else {
                stage.setFullScreen(true);
            }
        });
        headerBar.setContent(new FXHBox(button));
        VBox root = new VBox();
        root.getChildren().add(headerBar);
        root.getChildren().add(new FXLabel("1111"));
        Scene scene = new Scene(root, (double) 800.0F, (double) 600.0F);
        stage.initStyle(StageStyle.EXTENDED);
        stage.setScene(scene);
        // stage.setOnCloseRequest((e) -> windowsButtons.uninstall(headerBar, stage));
        stage.show();
    }

    public static class CustomTitleBar1Starter {
        public static void main(String[] args) {
            CustomTitleBarTest1.main(args);
        }
    }
}
