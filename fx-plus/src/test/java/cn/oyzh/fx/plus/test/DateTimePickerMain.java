package cn.oyzh.fx.plus.test;

import cn.oyzh.fx.plus.controls.date.DateTimePicker;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDateTime;

public class DateTimePickerMain extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        DateTimePicker dateTimePicker = new DateTimePicker();

//		dateTimePicker.setTimeProperty( LocalDateTime.now() );

        dateTimePicker.setShowLocalizedDateTime(false);


        final VBox vBox = new VBox();
        vBox.getChildren().add(dateTimePicker);
        final Scene scene = new Scene(vBox);
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.show();


        Thread thread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        if (dateTimePicker.dateTimeProperty() == null) {
                            System.out.println("û��ѡ�����ڣ�");
                        } else {
                            //��ȡѡ��ʱ��
                            LocalDateTime dateTime = dateTimePicker.dateTimeProperty().get();
                            System.out.println(dateTime.getYear() + "-" + dateTime.getMonthValue() + "-" + dateTime.getDayOfMonth() + " " + dateTime.getHour() + ":" + dateTime.getMinute() + ":" + dateTime.getSecond());
                        }
                        Thread.sleep(1000);
                    } catch (Exception e) {
//						throw new RuntimeException(e);
                        continue;
                    }
                }
            }
        };
        thread.start();
    }

    public static void main(String[] args) {
        launch(args);
    }


    static class DateTimePickerTest {
        public static void main(String[] args) {
            launch(DateTimePickerMain.class, args);
        }
    }

}
