// package cn.oyzh.fx.plus.test;
//
// import cn.oyzh.fx.plus.controls.calendar.TimePicker;
// import cn.oyzh.fx.plus.spring.SpringApplication;
// import javafx.scene.Scene;
// import javafx.scene.layout.LVBox;
// import javafx.stage.Stage;
//
// public class TimePickerTest extends SpringApplication {
//
//     public static void main(String[] args) {
//         launchSpring(TimePickerTest.class, args);
//     }
//
//     @Override
//     public void start(Stage primaryStage) {
//         TimePicker picker = new TimePicker();
//
// //		picker.setTimeProperty( LocalDateTime.now() );
//
//         // picker.setShowNow(false);
//
//         final LVBox vBox = new LVBox();
//         vBox.getChildren().add(picker);
//         final Scene scene = new Scene(vBox);
//         primaryStage.setScene(scene);
//         primaryStage.sizeToScene();
//         primaryStage.show();
//     }
//
//     @Override
//     public void destroy() throws Exception {
//
//     }
//
//     @Override
//     public void run(String... args) throws Exception {
//
//     }
//
//     static class TimePickerMain {
//         public static void main(String[] args) {
//             TimePickerTest.main(args);
//         }
//     }
//
// }
