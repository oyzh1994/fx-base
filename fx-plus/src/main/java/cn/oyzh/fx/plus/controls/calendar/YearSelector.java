// package cn.oyzh.fx.plus.controls.calendar;
//
// import cn.oyzh.fx.plus.controls.box.FXHBox;
// import cn.oyzh.fx.plus.controls.combo.FXComboBox;
// import cn.oyzh.fx.plus.controls.text.FXLabel;
// import javafx.event.ActionEvent;
// import javafx.geometry.Insets;
// import javafx.geometry.Pos;
// import javafx.scene.layout.HBox;
//
// import java.time.LocalDateTime;
//
// /**
//  * @author oyzh
//  * @since 2023/12/24
//  */
// public class YearSelector extends CalendarSelector {
//
//     /**
//      * 年下拉框
//      */
//     private FXComboBox<Integer> year;
//
//     public YearSelector(CalendarPicker<?> picker) {
//         super(picker);
//     }
//
//     @Override
//     protected void initCalendar() {
//         // 时间组件
//         FXHBox timeAction = new FXHBox();
//         timeAction.setAlignment(Pos.CENTER);
//         timeAction.setRealHeight(40);
//         timeAction.setPadding(new Insets(5, 0, 5, 0));
//
//         // 小时下拉框
//         this.year = new FXComboBox<>();
//         this.year.setRealWidth(180);
//
//         // 年文本框
//         FXLabel labelYear = new FXLabel("年");
//
//         // 设置边距
//         HBox.setMargin(labelYear, new Insets(0, 5, 0, 5));
//
//         timeAction.getChildren().add(this.year);
//         timeAction.getChildren().add(labelYear);
//
//         // 添加子节点
//         this.addChild(timeAction);
//
//         // 调用父类
//         super.initNode();
//     }
//
//     @Override
//     protected void initListener() {
//         super.initListener();
//         // 初始化事件
//         this.year.valueProperty().addListener((observable, oldValue, newValue) -> this.updateTime());
//     }
//
//     @Override
//     protected void updateCalendar() {
//         LocalDateTime time = this.picker.getValue();
//         time = time != null ? time : LocalDateTime.now();
//         Integer year = time.getYear();
//         this.year.select(year);
//     }
//
//     @Override
//     protected void initTime() {
//         for (int i = 1901; i <= 2155; i++) {
//             this.year.getItems().add(i);
//         }
//     }
//
//     @Override
//     protected void updateTime() {
//         LocalDateTime dateTime = LocalDateTime.now();
//         dateTime = dateTime.withYear(this.year.getSelectedItem());
//         this.picker.setValue(dateTime);
//     }
//
//     @Override
//     protected void onClearAction(ActionEvent event) {
//         this.picker.setValue((LocalDateTime) null);
//     }
//
//     @Override
//     protected void onNowAction(ActionEvent event) {
//         Integer year = LocalDateTime.now().getYear();
//         this.year.select(year);
//     }
// }
