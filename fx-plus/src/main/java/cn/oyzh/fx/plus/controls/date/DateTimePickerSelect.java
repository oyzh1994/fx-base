package cn.oyzh.fx.plus.controls.date;

import cn.oyzh.fx.plus.util.ResourceUtil;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.ResourceBundle;

/**
 * @author oyzh
 * @since 2023/12/24
 */
class DateTimePickerSelect extends VBox   {

    private final DateTimePicker dateTimePicker;

    private final List<Button> dayList;

    private Calendar calendar;

    private LocalDateTime cursorDateTime;

    private final FlowPane flowPane;

    private final FlowPane flow;

    private final Label labelYear;

    private final Label labelMouth;

    private final Button previousYear;

    private final Button nextYear;

    private final Button previousMonth;

    private final Button nextMonth;

    private final ComboBox<String> hour;

    private final ComboBox<String> minute;

    private final ComboBox<String> second;

    private final Button buttonCancel;

    private final Button buttonNow;

    private final Button buttonOK;

    private final Button buttonReset;

    public DateTimePickerSelect(final DateTimePicker parentCont) {
        this.dateTimePicker = parentCont;
        dayList = new ArrayList<Button>();

        this.setStyle("-fx-background-color: #efefef;");

        HBox ch1 = new HBox();
        ch1.setSpacing(10);
        ch1.setAlignment(Pos.CENTER);
        this.getChildren().add(ch1);

        this.previousYear = new Button("《");
        this.previousYear.setMaxHeight(20);
        this.previousYear.setPrefHeight(20);
        this.previousYear.setMinHeight(20);
        this.previousYear.setOnAction(this::previousYear);
        this.previousYear.setStyle("-fx-text-fill: black;-fx-background-color: #6cc;-fx-cursor: HAND;-fx-font-size: 10");
        ch1.getChildren().add(this.previousYear);

        this.previousMonth = new Button("←");
        this.previousMonth.setMaxHeight(20);
        this.previousMonth.setPrefHeight(20);
        this.previousMonth.setMinHeight(20);
        this.previousMonth.setOnAction(this::previousMonth);
        this.previousMonth.setStyle("-fx-text-fill: black;-fx-background-color: #c66;-fx-cursor: HAND;-fx-font-size: 10");
        ch1.getChildren().add(this.previousMonth);

        this.labelYear = new Label();
        this.labelYear.setId("fontStyle");
        ch1.getChildren().add(this.labelYear);

        Label label = new Label("年");
        label.setId("fontStyle");
        ch1.getChildren().add(label);

        this.labelMouth = new Label();
        this.labelMouth.setId("fontStyle");
        this.labelMouth.setMaxHeight(25);
        this.labelMouth.setPrefHeight(25);
        this.labelMouth.setMinHeight(25);
        ch1.getChildren().add(this.labelMouth);

        Label label1 = new Label("月");
        label1.setId("fontStyle");
        ch1.getChildren().add(label1);

        this.nextMonth = new Button("→");
        this.nextMonth.setMaxHeight(20);
        this.nextMonth.setPrefHeight(20);
        this.nextMonth.setMinHeight(20);
        this.nextMonth.setOnAction(this::nextMonth);
        this.nextMonth.setStyle("-fx-text-fill: black;-fx-background-color: #c66;-fx-cursor: HAND;-fx-font-size: 10");
        ch1.getChildren().add(this.nextMonth);

        this.nextYear = new Button("》");
        this.nextYear.setMaxHeight(20);
        this.nextYear.setPrefHeight(20);
        this.nextYear.setMinHeight(20);
        this.nextYear.setOnAction(this::previousMonth);
        this.nextYear.setStyle("-fx-text-fill: black;-fx-background-color: #6cc;-fx-cursor: HAND;-fx-font-size: 10");
        ch1.getChildren().add(this.nextYear);


        this.flowPane = new FlowPane();
        this.flowPane.setAlignment(Pos.CENTER);
        this.flowPane.setMaxHeight(25);
        this.flowPane.setPrefHeight(25);
        this.flowPane.setMinHeight(25);
        this.flowPane.setHgap(29);
        this.flowPane.setVgap(3);
        this.flowPane.setPrefWrapLength(260);
        this.getChildren().add(this.flowPane);

        Label labelD1 = new Label("一");
        labelD1.setStyle("-fx-font-size: 9;-fx-font-family: Microsoft YaHei;");
        this.flowPane.getChildren().add(labelD1);

        Label labelD2 = new Label("二");
        labelD2.setStyle("-fx-font-size: 9;-fx-font-family: Microsoft YaHei;");
        this.flowPane.getChildren().add(labelD2);

        Label labelD3 = new Label("三");
        labelD3.setStyle("-fx-font-size: 9;-fx-font-family: Microsoft YaHei;");
        this.flowPane.getChildren().add(labelD3);

        Label labelD4 = new Label("四");
        labelD4.setStyle("-fx-font-size: 9;-fx-font-family: Microsoft YaHei;");
        this.flowPane.getChildren().add(labelD4);

        Label labelD5 = new Label("五");
        labelD5.setStyle("-fx-font-size: 9;-fx-font-family: Microsoft YaHei;");
        this.flowPane.getChildren().add(labelD5);

        Label labelD6 = new Label("六");
        labelD6.setStyle("-fx-font-size: 9;-fx-font-family: Microsoft YaHei;");
        this.flowPane.getChildren().add(labelD6);

        Label labelD7 = new Label("七");
        labelD7.setStyle("-fx-font-size: 9;-fx-font-family: Microsoft YaHei;");
        this.flowPane.getChildren().add(labelD7);

        this.flow = new FlowPane();
        this.flow.setAlignment(Pos.CENTER);
        this.flow.setHgap(12);
        this.flow.setVgap(6);
        this.flow.setStyle("-fx-cursor: HAND;");
        this.flow.setPrefWrapLength(260);
        this.getChildren().add(this.flow);

        HBox ch4 = new HBox();
        ch4.setAlignment(Pos.CENTER);
        ch4.setPrefHeight(30);
        ch4.setMinHeight(30);
        ch4.setMaxHeight(30);
        this.getChildren().add(ch4);

        this.hour = new ComboBox<>();
        this.hour.setId("fontStyle");
        ch4.getChildren().add(hour);

        Label label2 = new Label("时");
        label2.setId("fontStyle");
        ch4.getChildren().add(label2);

        this.minute = new ComboBox<>();
        this.minute.setId("fontStyle");
        ch4.getChildren().add(minute);

        Label label3 = new Label("分");
        label3.setId("fontStyle");
        ch4.getChildren().add(label3);

        this.second = new ComboBox<>();
        this.second.setId("fontStyle");
        ch4.getChildren().add(second);

        Label label4 = new Label("秒");
        label4.setId("fontStyle");
        ch4.getChildren().add(label4);

        HBox ch5 = new HBox();
        ch5.setAlignment(Pos.BASELINE_CENTER);
        this.getChildren().add(ch5);

        this.buttonReset = new Button("清空");
        buttonReset.setOnAction(this::buttonResetOnAction);
        buttonReset.setOnMousePressed(this::buttonResetOnMousePressed);
        buttonReset.setOnMouseReleased(this::buttonResetOnMouseReleased);
        buttonReset.setMaxWidth(58.6);
        buttonReset.setPrefWidth(58.6);
        buttonReset.setMinHeight(25);
        buttonReset.setMaxHeight(25);
        buttonReset.setStyle("-fx-background-color:#96e561;-fx-font-size: 12;-fx-cursor: HAND;");
        ch5.getChildren().add(buttonReset);

        Label label5 = new Label();
        label5.setPrefWidth(5);
        ch5.getChildren().add(label5);

        this.buttonCancel = new Button("取消");
        buttonCancel.setOnAction(this::buttonCancelOnAction);
        buttonCancel.setOnMousePressed(this::buttonCancelOnMousePressed);
        buttonCancel.setOnMouseReleased(this::buttonCancelOnMouseReleased);
        buttonCancel.setMaxWidth(58.6);
        buttonCancel.setPrefWidth(58.6);
        buttonCancel.setMinHeight(25);
        buttonCancel.setMaxHeight(25);
        buttonCancel.setStyle("-fx-background-color:#FFD2D2;-fx-font-size: 12;-fx-cursor: HAND;");
        ch5.getChildren().add(buttonCancel);

        Label label6 = new Label();
        label6.setPrefWidth(5);
        ch5.getChildren().add(label6);

        this.buttonNow = new Button("此刻");
        buttonNow.setOnAction(this::buttonNowOnAction);
        buttonNow.setOnMousePressed(this::buttonNowOnMousePressed);
        buttonNow.setOnMouseReleased(this::buttonNowOnMouseReleased);
        buttonNow.setMaxWidth(58.6);
        buttonNow.setPrefWidth(58.6);
        buttonNow.setMinHeight(25);
        buttonNow.setMaxHeight(25);
        buttonNow.setStyle("-fx-background-color:#FFD2D2;-fx-font-size: 12;-fx-cursor: HAND;");
        ch5.getChildren().add(buttonNow);

        Label label7 = new Label();
        label7.setPrefWidth(5);
        ch5.getChildren().add(label7);

        this.buttonOK = new Button("确认");
        buttonOK.setOnAction(this::buttonOKOnAction);
        buttonOK.setOnMousePressed(this::buttonOKOnMousePressed);
        buttonOK.setOnMouseReleased(this::buttonOKOnMouseReleased);
        buttonOK.setMaxWidth(58.6);
        buttonOK.setPrefWidth(58.6);
        buttonOK.setMinHeight(25);
        buttonOK.setMaxHeight(25);
        buttonOK.setStyle("-fx-background-color:#ACD6FF;-fx-font-size: 12;-fx-cursor: HAND;");
        ch5.getChildren().add(buttonOK);

        Label ch6 = new Label();
        ch6.setMinHeight(5);
        ch6.setPrefHeight(5);
        this.getChildren().add(ch6);

        if (this.dateTimePicker.dateTimeProperty() != null) {
            this.cursorDateTime = this.dateTimePicker.dateTimeProperty().getValue();
        }
        //生成日期部分
        upDataCalendar(true);
    }

    public String strValue(int i) {
        String res;
        if (i < 10) {
            res = "0" + i;
        } else {
            res = i + "";
        }
        return res;
    }

    /**
     * 用于更新年月展示
     */
    public void upDataLab() {
        if (calendar != null) {
            labelMouth.setText(calendar.get(Calendar.MONTH) + 1 + "");
            labelYear.setText(calendar.get(Calendar.YEAR) + "");
        }
    }

    /**
     * 更新日期部分的数据
     */
    public void upDataCalendar(boolean open) {
        dayList.clear();
        flow.getChildren().clear();

        this.cursorDateTime = this.dateTimePicker.dateTimeProperty() != null ? this.dateTimePicker.dateTimeProperty().get() : null;
        int nowYear = this.cursorDateTime != null ? this.cursorDateTime.getYear() : -1;
        int nowMonth = this.cursorDateTime != null ? this.cursorDateTime.getMonthValue() : -1;

        Calendar tmpCalendar = this.cursorDateTime != null ?
                GregorianCalendar.from(ZonedDateTime.of(this.cursorDateTime, ZoneId.systemDefault())) :
                GregorianCalendar.from(ZonedDateTime.of(LocalDateTime.now(), ZoneId.systemDefault()));
        if (open) {
            calendar = tmpCalendar;
        } else {
            tmpCalendar = this.calendar;
        }

        int mouthDays = tmpCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        tmpCalendar.set(tmpCalendar.get(Calendar.YEAR), tmpCalendar.get(Calendar.MONTH), mouthDays, tmpCalendar.get(Calendar.HOUR_OF_DAY), tmpCalendar.get(Calendar.MINUTE), tmpCalendar.get(Calendar.SECOND));
        int weekMouthLastDay = tmpCalendar.get(Calendar.DAY_OF_WEEK);
        tmpCalendar.set(tmpCalendar.get(Calendar.YEAR), tmpCalendar.get(Calendar.MONTH), 1, tmpCalendar.get(Calendar.HOUR_OF_DAY), tmpCalendar.get(Calendar.MINUTE), tmpCalendar.get(Calendar.SECOND));
        int weekMouthFirstDay = tmpCalendar.get(Calendar.DAY_OF_WEEK);
        tmpCalendar.add(Calendar.MONTH, -1);
        int lastMouthDays = tmpCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        tmpCalendar.add(Calendar.MONTH, 1);

        //System.out.println("本月天数："+mouthDays+"   上月天数"+lastMouthDays);
        if (weekMouthFirstDay == 1) {
            //System.out.println("本月第一天是周日，前面有6天");
            for (int i = lastMouthDays - 5; i <= lastMouthDays; i++) {
                //dayList.add(i);
                Button btn = new Button(strValue(i));
                setDisable(btn);
                flow.getChildren().add(btn);
            }
            for (int i = 1; i <= mouthDays; i++) {
                Button btn = new Button(strValue(i));
                dayList.add(btn);
                setAble(btn);
                flow.getChildren().add(btn);
            }
        } else if (weekMouthFirstDay == 2) {
            //System.out.println("本月第一天是周一，前面没有");
            for (int i = 1; i <= mouthDays; i++) {
                Button btn = new Button(strValue(i));
                dayList.add(btn);
                setAble(btn);
                if (this.cursorDateTime != null && this.cursorDateTime.getDayOfMonth() == i && this.calendar != null &&
                        this.calendar.get(Calendar.YEAR) == nowYear && this.calendar.get(Calendar.MONTH) + 1 == nowMonth) {
                    btn.setStyle("-fx-text-fill: white;-fx-background-color: #5b8cff;-fx-font-size: 10");
                    this.calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), this.cursorDateTime.getDayOfMonth());
                }
                flow.getChildren().add(btn);
            }
        } else {
            //System.out.println("本月第一天不是周日，也不是周一");
            for (int i = lastMouthDays - weekMouthFirstDay + 3; i <= lastMouthDays; i++) {
                //dayList.add(i);
                Button btn = new Button(strValue(i));
                setDisable(btn);
                flow.getChildren().add(btn);
            }
            for (int i = 1; i <= mouthDays; i++) {
                Button btn = new Button(strValue(i));
                dayList.add(btn);
                setAble(btn);
                if (this.cursorDateTime != null && this.cursorDateTime.getDayOfMonth() == i && this.calendar != null &&
                        this.calendar.get(Calendar.YEAR) == nowYear && this.calendar.get(Calendar.MONTH) + 1 == nowMonth) {
                    btn.setStyle("-fx-text-fill: white;-fx-background-color: #5b8cff;-fx-font-size: 10");
                    this.calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), this.cursorDateTime.getDayOfMonth());
                }
                flow.getChildren().add(btn);
            }
        }
        if (weekMouthLastDay != 1) {
            for (int i = 1; i <= 8 - weekMouthLastDay; i++) {
                //dayList.add(i);
                Button btn = new Button(strValue(i));
                setDisable(btn);
                flow.getChildren().add(btn);
            }
        }
        //生成时间部分
        setTime();
        //显示当前年月
        upDataLab();
    }

    /**
     * 设置上月和下月在本月显示的日期样式，并设置为不可点击
     *
     * @param btn 日期按钮Button
     */
    public void setDisable(Button btn) {
        btn.setDisable(true);
        btn.setStyle("-fx-text-fill: black;-fx-background-color: transparent;;-fx-font-size: 10");
    }

    /**
     * 设置本月日期的点击事件和样式，其中点击时间后，自动记录时间
     *
     * @param btn 日期按钮Button
     */
    public void setAble(Button btn) {
        btn.setStyle("-fx-text-fill: black;-fx-background-color: #fff;-fx-font-size: 10");
        btn.setOnAction(event -> {
            dayList.forEach(e -> {
                e.setStyle("-fx-text-fill: black;-fx-background-color: #fff;-fx-font-size: 10");
            });
            btn.setStyle("-fx-text-fill: white;-fx-background-color: #5b8cff;-fx-font-size: 10");
            calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), Integer.valueOf(btn.getText()),
                    calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));
            ;
        });
    }

    public void previousYear(ActionEvent event) {
        calendar.add(Calendar.YEAR, -1);
        upDataCalendar(false);
    }

    public void previousMonth(ActionEvent event) {
        calendar.add(Calendar.MONTH, -1);
        upDataCalendar(false);
    }

    @FXML
    public void nextYear(ActionEvent event) {
        calendar.add(Calendar.YEAR, 1);
        upDataCalendar(false);
    }

    @FXML
    public void nextMonth(ActionEvent event) {
        calendar.add(Calendar.MONTH, 1);
        upDataCalendar(false);
    }

    private void setTime() {
        for (int i = 1; i < 25; i++) {
            hour.getItems().add(strValue(i));
        }
        hour.getSelectionModel().select(calendar.get(Calendar.HOUR_OF_DAY) == 0 ? 24 : calendar.get(Calendar.HOUR_OF_DAY) - 1);

        for (int i = 0; i < 60; i++) {
            minute.getItems().add(strValue(i));
        }
        minute.getSelectionModel().select(calendar.get(Calendar.MINUTE));

        for (int i = 0; i < 60; i++) {
            second.getItems().add(strValue(i));
        }
        second.getSelectionModel().select(calendar.get(Calendar.SECOND));
    }

    protected void buttonOKOnMousePressed(MouseEvent event) {
        buttonOK.setStyle("-fx-background-color:#FFFFB9;-fx-font-size: 12;-fx-cursor: HAND;");
    }

    protected void buttonOKOnMouseReleased(MouseEvent event) {
        buttonOK.setStyle("-fx-background-color:#ACD6FF;-fx-font-size: 12;-fx-cursor: HAND;");
    }

    protected void buttonOKOnAction(ActionEvent event) {
        if (this.calendar != null) {
            calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),
                    Integer.parseInt(hour.getSelectionModel().getSelectedItem()), Integer.parseInt(minute.getSelectionModel().getSelectedItem()), Integer.parseInt(second.getSelectionModel().getSelectedItem()));
            LocalDateTime localDateTime = LocalDateTime.ofInstant(calendar.toInstant(), ZoneId.systemDefault());
            this.dateTimePicker.setTimeProperty(localDateTime);
        } else {
//            System.out.println("请先选择日期");
        }
        this.dateTimePicker.hide();
    }

    protected void buttonNowOnMousePressed(MouseEvent event) {
        buttonOK.setStyle("-fx-background-color:#FFFFB9;-fx-font-size: 12;-fx-cursor: HAND;");
    }

    protected void buttonNowOnMouseReleased(MouseEvent event) {
        buttonOK.setStyle("-fx-background-color:#e7c269;-fx-font-size: 12;-fx-cursor: HAND;");
    }

    protected void buttonNowOnAction(ActionEvent event) {
        LocalDateTime localDateTime = LocalDateTime.now();
        calendar = Calendar.getInstance();
        if (this.calendar != null) {
            calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),
                    Integer.parseInt(hour.getSelectionModel().getSelectedItem()), Integer.parseInt(minute.getSelectionModel().getSelectedItem()), Integer.parseInt(second.getSelectionModel().getSelectedItem()));
        } else {
//            System.out.println("请先选择日期");
        }
        this.dateTimePicker.setTimeProperty(localDateTime);
        this.dateTimePicker.hide();
    }

    protected void buttonCancelOnMousePressed(MouseEvent event) {
        buttonCancel.setStyle("-fx-background-color:#FFFFB9;-fx-font-size: 12;-fx-cursor: HAND;");
    }

    protected void buttonCancelOnMouseReleased(MouseEvent event) {
        buttonCancel.setStyle("-fx-background-color:#FFD2D2;-fx-font-size: 12;-fx-cursor: HAND;");
    }

    protected void buttonCancelOnAction(ActionEvent event) {
        this.dateTimePicker.hide();
    }

    protected void buttonResetOnMousePressed(MouseEvent event) {
        buttonReset.setStyle("-fx-background-color:#FFFFB9;-fx-font-size: 12;-fx-cursor: HAND;");
    }

    protected void buttonResetOnMouseReleased(MouseEvent event) {
        buttonReset.setStyle("-fx-background-color:#96e561;-fx-font-size: 12;-fx-cursor: HAND;");
    }

    protected void buttonResetOnAction(ActionEvent event) {
        this.calendar = null;
        this.cursorDateTime = null;
        this.dateTimePicker.clearTimeProperty();
    }
//    /**
//     *
//     * 设置年月左右选择按钮被按下时和弹起时的颜色
//     * @param btn  年月左右选择按钮Button
//     */
//    public void btnMouthPress(Button btn){
//        btn.setOnMousePressed(new EventHandler<MouseEvent>() {
//            @Override
//            public void handle(MouseEvent event) {
//                btn.setStyle("-fx-text-fill: black;-fx-background-color: #FFD306;");
//            }
//        });
//        btn.setOnMouseReleased(new EventHandler<MouseEvent>() {
//            @Override
//            public void handle(MouseEvent event) {
//                btn.setStyle("-fx-text-fill: black;-fx-background-color: #c66;");
//            }
//        });
//    }
//    public void btnYearPress(Button btn){
//        btn.setOnMousePressed(new EventHandler<MouseEvent>() {
//            @Override
//            public void handle(MouseEvent event) {
//                btn.setStyle("-fx-text-fill: black;-fx-background-color:#FF0000;");
//            }
//        });
//        btn.setOnMouseReleased(new EventHandler<MouseEvent>() {
//            @Override
//            public void handle(MouseEvent event) {
//                btn.setStyle("-fx-text-fill: black;-fx-background-color: #6cc;");
//            }
//        });
//    }

    @Override
    public String getUserAgentStylesheet() {
        return ResourceUtil.toExternalUrl("/fx-plus/css/date-time-picker.css");
    }
}