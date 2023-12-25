package cn.oyzh.fx.plus.test;

import cn.oyzh.fx.plus.controls.date.DateTimePicker;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class DateTimePickerTest extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        DateTimePicker picker = new DateTimePicker();
        picker.setShowNow(false);

        final VBox vBox = new VBox();
        vBox.getChildren().add(picker);
        final Scene scene = new Scene(vBox);
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.show();
    }

    static class DateTimePickerMain {
        public static void main(String[] args) {
            launch(DateTimePickerTest.class, args);
        }
    }
}
