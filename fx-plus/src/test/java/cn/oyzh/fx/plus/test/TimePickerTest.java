package cn.oyzh.fx.plus.test;

import cn.oyzh.fx.plus.controls.date.DateTimePicker;
import cn.oyzh.fx.plus.controls.date.TimePicker;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDateTime;

public class TimePickerTest extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        TimePicker picker = new TimePicker();

//		picker.setTimeProperty( LocalDateTime.now() );

        picker.setShowLocalizedDateTime(false);

        final VBox vBox = new VBox();
        vBox.getChildren().add(picker);
        final Scene scene = new Scene(vBox);
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.show();
    }

    static class TimePickerMain {
        public static void main(String[] args) {
            launch(TimePickerTest.class, args);
        }
    }

}
