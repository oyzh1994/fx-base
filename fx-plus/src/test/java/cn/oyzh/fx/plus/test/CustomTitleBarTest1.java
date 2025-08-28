package cn.oyzh.fx.plus.test;

import cn.oyzh.fx.plus.controls.FXHeaderBar;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.theme.Themes;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Set;

/**
 * @author oyzh
 * @since 2025-08-18
 */
public class CustomTitleBarTest1 extends Application {

    public static void main(String[] args) {
        System.setProperty("javafx.enablePreview", "true");
        // System.setProperty("javafx.suppressPreviewWarning", "true");
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        // ThemeManager.apply(Themes.PRIMER_LIGHT);
        // ThemeManager.apply(Themes.PRIMER_DARK);
        // Application.setUserAgentStylesheet(ThemeManager.currentUserAgentStylesheet());
        FXHeaderBar headerBar = new FXHeaderBar();
        Button button1 = new Button("test1");
        Button button2 = new Button("test2");
        Button button3 = new Button("test3");
        Button button4 = new Button("test4");
        Button button5 = new Button("test5");
        Button button6 = new Button("test6");
        Button button7 = new Button("test7");
        Button button8 = new Button("test8");
        Button button9 = new Button("test9");
        FXHBox hBox = new FXHBox();
        hBox.addChild(button1);
        hBox.addChild(button2);
        hBox.addChild(button3);
        hBox.addChild(button4);
        hBox.addChild(button5);
        hBox.addChild(button6);
        hBox.addChild(button7);
        hBox.addChild(button8);
        hBox.addChild(button9);
        headerBar.setContent(hBox);
        headerBar.setTitle("测试标题");
        headerBar.setStyle("-fx-background-color: red;-fx-text-fill: red;-fx-fill: red");

        System.out.println(headerBar.getStyle());
        Set<Node> nodes= headerBar.lookupAll("*");
        for (Node node : nodes) {
            System.out.println(node);
        }
        VBox root = new VBox();
        root.getChildren().add(headerBar);
        root.getChildren().add(new FXLabel("1111"));

        Scene scene = new Scene(root, (double) 800.0F, (double) 600.0F);
        scene.setFill(Color.TRANSPARENT);
        stage.initStyle(StageStyle.EXTENDED);
        stage.setScene(scene);
        stage.show();

        stage.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            System.out.println(event.getTarget());
        });
    }

    public static class CustomTitleBar1Starter {
        public static void main(String[] args) {
            CustomTitleBarTest1.main(args);
        }
    }
}
