package cn.oyzh.fx.plus.test;

import cn.oyzh.fx.plus.titlebar.TitleBar;
import cn.oyzh.fx.plus.titlebar.TitleBox;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * @author oyzh
 * @since 2024-11-15
 */
public class TitleBarTest extends Application {

    @Override
    public void start(Stage primaryStage) {
        TitleBar.TitleBarConfig config = new TitleBar.TitleBarConfig();
        config.setIcon( "/zoo_no_bg.png");
        TitleBar bar = new TitleBar(config);
        TitleBox vbox = new TitleBox();
        vbox.setTitleBar(bar);
        vbox.setPrefWidth(500);
        vbox.setPrefHeight(300);

        vbox.setOpacity(1);

        FXHBox hbox = new FXHBox(new Button("测试1"),new Button("测试2"));

        vbox.setContent(hbox);

//        vbox.setOnMouseMoved(new EventHandler<MouseEvent>() {
//            @Override
//            public void handle(MouseEvent event) {
//                double x = event.getX();
//                double windowX = vbox.getScene().getWindow().getX();
//                System.out.println(windowX);
//                System.out.println(x);
//                if (x - vbox.realWidth() < 5) {
//                    vbox.setCursor(Cursor.W_RESIZE);
//                } else {
//                    vbox.setCursor(Cursor.DEFAULT);
//                }
//            }
//        });
        Scene scene = new Scene(vbox);
        primaryStage.setScene(scene);
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.show();
    }

    public static void main(String[] args) {
        Application.launch(TitleBarTest.class, args);
    }

    public static class TitleBarMain {
        public static void main(String[] args) {
            TitleBarTest.main(args);
        }

    }
}

