// package cn.oyzh.fx.plus.controls.calendar;
//
// import cn.oyzh.fx.plus.controls.box.FXHBox;
// import cn.oyzh.fx.plus.controls.FXVBox;
// import cn.oyzh.fx.plus.controls.button.FXButton;
// import javafx.event.ActionEvent;
// import javafx.geometry.Insets;
// import javafx.geometry.Pos;
// import javafx.scene.layout.HBox;
//
// /**
//  * @author oyzh
//  * @since 2023/12/26
//  */
// public abstract class CalendarSelector extends FXVBox {
//
//     /**
//      * 清除
//      */
//     protected FXButton buttonClear;
//
//     /**
//      * 当前
//      */
//     protected FXButton buttonNow;
//
//     /**
//      * 关闭
//      */
//     protected FXButton buttonClose;
//
//     /**
//      * 底部操作组件
//      */
//     protected FXHBox bottomAction;
//
//     /**
//      * 日历选择器
//      */
//     protected CalendarPicker<?> picker;
//
//     public CalendarSelector(CalendarPicker<?> picker) {
//         this.picker = picker;
//         // 初始化节点
//         this.initCalendar();
//         // 初始化时间
//         this.initTime();
//         // 更新日期
//         this.updateCalendar();
//         // 初始化监听器
//         this.initListener();
//     }
//
//     /**
//      * 初始化监听器
//      */
//     protected void initListener() {
//
//     }
//
//     /**
//      * 初始化节点
//      */
//     protected void initCalendar() {
//         this.bottomAction = new FXHBox();
//         this.bottomAction.setAlignment(Pos.BASELINE_CENTER);
//
//         // 清除
//         this.buttonClear = new FXButton("清除");
//         this.buttonClear.getStyleClass().add("danger");
//         this.buttonClear.setOnAction(this::onClearAction);
//         this.buttonClear.setRealWidth(60);
//         this.buttonClear.setRealHeight(25);
//
//         // 当前
//         this.buttonNow = new FXButton("当前");
//         this.buttonNow.getStyleClass().add("success");
//         this.buttonNow.setOnAction(this::onNowAction);
//         this.buttonNow.setRealWidth(60);
//         this.buttonNow.setRealHeight(25);
//
//         // 关闭
//         this.buttonClose = new FXButton("关闭");
//         this.buttonClose.getStyleClass().add("default");
//         this.buttonClose.setOnAction(this::onCloseAction);
//         this.buttonClose.setRealWidth(60);
//         this.buttonClose.setRealHeight(25);
//
//         HBox.setMargin(this.buttonClear, new Insets(0, 5, 0, 0));
//         HBox.setMargin(this.buttonNow, new Insets(0, 5, 0, 0));
//
//         this.bottomAction.getChildren().add(this.buttonClear);
//         this.bottomAction.getChildren().add(this.buttonNow);
//         this.bottomAction.getChildren().add(this.buttonClose);
//
//         // 添加子节点
//         this.addChild(this.bottomAction);
//     }
//
//
//     /**
//      * 清除按钮事件
//      *
//      * @param event 事件
//      */
//     protected void onClearAction(ActionEvent event) {
//
//     }
//
//     /**
//      * 当前按钮事件
//      *
//      * @param event 事件
//      */
//     protected void onNowAction(ActionEvent event) {
//
//     }
//
//     /**
//      * 关闭按钮事件
//      *
//      * @param event 事件
//      */
//     protected void onCloseAction(ActionEvent event) {
//         this.picker.hidePopup();
//     }
//
//     /**
//      * 更新日期
//      */
//     protected void updateCalendar() {
//     }
//
//     /**
//      * 初始化时间
//      */
//     protected void initTime() {
//     }
//
//     /**
//      * 更新时间
//      */
//     protected void updateTime() {
//     }
// }
