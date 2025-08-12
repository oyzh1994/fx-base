package cn.oyzh.fx.plus.test;

import cn.oyzh.fx.plus.LimitLenControl;
import cn.oyzh.fx.plus.controls.text.field.FXTextField;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * @author oyzh
 * @since 2025-06-12
 */
public class AppTestMain extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        // test1(stage);
        // test2(stage);
        test3(stage);
    }

    private void test1(Stage stage) {
        HBox hBox = new HBox();
        ToolBar toolBar = new ToolBar();
        toolBar.getItems().add(new Button("test1"));
        toolBar.getItems().add(new Button("test2"));
        toolBar.getItems().add(new Button("test3"));
        toolBar.getItems().add(new Button("test4"));
        toolBar.getItems().add(new CheckBox("test5"));
        toolBar.getItems().add(new CheckBox("test6"));

        hBox.getChildren().add(toolBar);
        stage.setScene(new Scene(hBox));
        stage.show();
    }

    private void test2(Stage stage) {
        // 创建一个轻量级HBox
        LightweightHBox hbox = new LightweightHBox(10);
        hbox.setPadding(new javafx.geometry.Insets(10));

        Button btn1 = new Button("Button 1");
        Button btn2 = new Button("Button 2");
        Label label = new Label("这是一个标签");

        // 设置按钮可以增长
        LightweightHBox.setHgrow(btn1, Priority.ALWAYS);
        LightweightHBox.setHgrow(btn2, Priority.ALWAYS);

        hbox.getChildren().addAll(btn1, label, btn2);

        // 创建一个轻量级VBox
        LightweightVBox vbox = new LightweightVBox(15);
        vbox.setPadding(new javafx.geometry.Insets(15));

        Label title = new Label("轻量级布局示例");
        title.setStyle("-fx-font-size: 16pt; -fx-font-weight: bold;");

        vbox.getChildren().addAll(title, hbox);

        Scene scene = new Scene(vbox, 400, 300);
        stage.setTitle("轻量级布局容器示例");
        stage.setScene(scene);
        stage.show();
    }

    private void test3(Stage stage) {
        // 创建一个轻量级HBox
        FXTextField field = new FXTextField();
        field.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                System.out.println(newValue);
            }
        });

        VBox vbox = new VBox();
        vbox.getChildren().addAll(field);

        Scene scene = new Scene(vbox, 400, 300);
        stage.setTitle("轻量级布局容器示例");
        stage.setScene(scene);
        stage.show();
    }

    public static class AppTestMainApp {

        public static void main(String[] args) {
            AppTestMain.main(args);
        }
    }
}
