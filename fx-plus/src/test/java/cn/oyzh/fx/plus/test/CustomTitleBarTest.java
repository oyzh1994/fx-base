package cn.oyzh.fx.plus.test;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HeaderBar;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
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
        Button button = new Button("test");
        button.setOnAction(event -> {
            if (stage.isFullScreen()) {
                stage.setFullScreen(false);
            } else {
                stage.setFullScreen(true);
            }
        });
        Button button2 = new Button("test2");
        Button button3 = new Button("test3");
        Button button4 = new Button("test4");
        Button button5 = new Button("test5");
        Button button6 = new Button("test6");
        Button button7 = new Button("test7");
        Button button8 = new Button("test8");

        HBox hBox = new HBox(button, button2, button3, button4, button5, button6, button7, button8);
        // hBox.setMouseTransparent(true);
        hBox.setStyle("-fx-background-color:f00f");
        hBox.setMaxWidth(Double.MAX_VALUE);
        hBox.setMouseTransparent(true);
        headerBar.setLeading(hBox);

        Label label = new Label("标题");
        label.setMouseTransparent(true);
        label.setPrefWidth(2000);
        headerBar.setTrailing(label);

        // headerBar.setMouseTransparent(false);
        // headerBar.setTrailingSystemPadding(true);
        // headerBar.setLeadingSystemPadding(true);
        // headerBar.setLeading(hBox);
        // HeaderButtonGroup windowsButtons = HeaderButtonGroup.standardGroup();
        // windowsButtons.install(headerBar, stage);
        Pane root = new Pane();
        // root.setMouseTransparent(true);
        // root.setPickOnBounds(false);
        // root.setTop(headerBar);
        root.getChildren().add(headerBar);
        root.getChildren().add(new Label("1111"));
        Scene scene = new Scene(root, (double) 800.0F, (double) 600.0F);
        // scene.getStylesheets().add(Decoration.GENOME_LIGHT.getStylesheet());
        stage.initStyle(StageStyle.EXTENDED);
        stage.setScene(scene);
        scene.setFill(Color.TRANSPARENT);
        stage.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            System.out.println(event.getTarget());
        });
        // stage.setOnCloseRequest((e) -> windowsButtons.uninstall(headerBar, stage));
        stage.show();
    }

    public static class CustomTitleBarStarter {
        public static void main(String[] args) {
            CustomTitleBarTest.main(args);
        }
    }
}
