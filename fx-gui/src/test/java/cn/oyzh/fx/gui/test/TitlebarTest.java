package cn.oyzh.fx.gui.test;

import cn.oyzh.fx.gui.titlebar.TitleBar;
import cn.oyzh.fx.gui.titlebar.TitleBarConfig;
import cn.oyzh.fx.gui.titlebar.TitleBox;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.ext.FXApplication;
import cn.oyzh.fx.plus.mouse.MouseEventHandler;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * @author oyzh
 * @since 2024-11-15
 */
public class TitlebarTest extends Application {

    @Override
    public void start(Stage primaryStage) {
        TitleBarConfig config = TitleBarConfig.ofPlatformCommon("", "");
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
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.show();
    }

    public static void main(String[] args) {
        Application.launch(TitlebarTest.class, args);
    }

    public static class TitlebarMain {
        public static void main(String[] args) {
            TitlebarTest.main(args);
        }

    }
}

