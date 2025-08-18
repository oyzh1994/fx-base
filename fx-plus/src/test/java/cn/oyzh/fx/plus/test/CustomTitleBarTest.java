package cn.oyzh.fx.plus.test;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HeaderBar;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * @author oyzh
 * @since 2025-08-18
 */
public class CustomTitleBarTest extends Application {

    public static void main(String[] args) {
        System.setProperty("javafx.enablePreview", "true");
        System.setProperty("javafx.suppressPreviewWarning", "true");
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        HeaderBar headerBar = new HeaderBar();
       Button button= new Button("test");
        button.setOnAction(event -> {
            if(stage.isFullScreen()){
                stage.setFullScreen(false);
            }else {
                stage.setFullScreen(true);
            }
        });
        headerBar.setCenter(button);
        // HeaderButtonGroup windowsButtons = HeaderButtonGroup.standardGroup();
        // windowsButtons.install(headerBar, stage);
        BorderPane root = new BorderPane();
        root.setTop(headerBar);
        Scene scene = new Scene(root, (double) 800.0F, (double) 600.0F);
        // scene.getStylesheets().add(Decoration.GENOME_LIGHT.getStylesheet());
        stage.initStyle(StageStyle.EXTENDED);
        stage.setScene(scene);
        // stage.setOnCloseRequest((e) -> windowsButtons.uninstall(headerBar, stage));
        stage.show();
    }

    public static class CustomTitleBarStarter {
        public static void main(String[] args) {
            CustomTitleBarTest.main(args);
        }
    }
}
