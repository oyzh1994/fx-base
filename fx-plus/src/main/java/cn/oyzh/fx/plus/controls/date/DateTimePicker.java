package cn.oyzh.fx.plus.controls.date;

import cn.oyzh.fx.plus.controls.text.FlexLabel;
import cn.oyzh.fx.plus.controls.textfield.ClearableTextField;
import cn.oyzh.fx.plus.util.ResourceUtil;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Window;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Objects;

/**
 * @author oyzh
 * @since 2023/12/24
 */
public class DateTimePicker extends HBox {

    /**
     * 格式化器
     */
    private final DateTimeFormatter formatter;

    /**
     * 时间值
     */
    private ObjectProperty<LocalDateTime> valueProperty;

    /**
     * 弹出组件
     */
    private Popup popup;

    /**
     * 选择器
     */
    private DateTimePickerSelect select;

    /**
     * 是否显示当前时间
     */
    @Getter
    public boolean showNow;

    /**
     * 按钮组件
     */
    private final Button button;

    /**
     * 文本组件
     */
    private final ClearableTextField textField;

    public DateTimePicker() {
        // 初始化文本组件
        this.textField = new ClearableTextField();
        this.textField.setFocusTraversable(false);
        this.textField.maxHeightProperty().bind(this.maxHeightProperty());
        this.textField.minHeightProperty().bind(this.minHeightProperty());
        this.textField.prefHeightProperty().bind(this.prefHeightProperty());
        this.textField.setId("dt_text_field");

        // 初始化按钮组件
        this.button = new Button();
        this.button.setId("dt_button");
        this.button.maxHeightProperty().bind(this.maxHeightProperty());
        this.button.minHeightProperty().bind(this.minHeightProperty());
        this.button.prefHeightProperty().bind(this.prefHeightProperty());
        this.button.setGraphic(new ImageView("/fx-plus/img/calendar.png"));

        // 初始化格式化器
        this.formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // 初始化监听器
        this.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                this.textField.clear();
            } else {
                this.textField.setText(this.formatter.format(newValue));
            }
        });
        this.button.setOnAction(this::handleButtonAction);

        // 添加到子节点列表
        this.getChildren().add(this.textField);
        this.getChildren().add(this.button);
    }

    /**
     * 设置提示文本
     *
     * @param promptText 提示文本
     */
    public void setPromptText(String promptText) {
        this.textField.setPromptText(promptText);
    }

    /**
     * 获取提示文本
     *
     * @return 提示文本
     */
    public String setPromptText() {
        return this.textField.getPromptText();
    }

    /**
     * 获取值属性
     *
     * @return 值属性
     */
    public ObjectProperty<LocalDateTime> valueProperty() {
        if (this.valueProperty == null) {
            this.valueProperty = new SimpleObjectProperty<>();
        }
        return this.valueProperty;
    }

    /**
     * 获取值
     *
     * @return 值
     */
    public LocalDateTime getValue() {
        return this.valueProperty == null ? null : this.valueProperty.get();
    }

    /**
     * 设置值
     *
     * @param value 值
     */
    public void setValue(LocalDateTime value) {
        this.valueProperty().setValue(value);
    }

    /**
     * 清除值
     */
    public void clearValue() {
        this.valueProperty().set(null);
    }

    /**
     * 设置是否显示当前时间
     *
     * @param showNow 是否显示当前时间
     */
    public void setShowNow(boolean showNow) {
        if (showNow) {
            this.valueProperty.setValue(LocalDateTime.now());
        } else {
            this.valueProperty.setValue(null);
        }
        this.showNow = showNow;
    }

    /**
     * 处理按钮事件
     *
     * @param event 事件
     */
    protected void handleButtonAction(ActionEvent event) {
        // 隐藏弹窗
        if (this.popup != null && popup.isShowing()) {
            this.popup.hide();
        } else {
            // 初始化弹窗
            if (this.popup == null) {
                this.popup = new Popup();
                this.select = new DateTimePickerSelect();
                this.popup.getContent().add(this.select);
                this.popup.autoHideProperty().set(true);
            }
            // 获取window对象
            final Window window = this.button.getScene().getWindow();
            // 计算x值
            final double x = window.getX() + this.textField.localToScene(0, 0).getX() + this.textField.getScene().getX();
            // 计算y值
            final double y = window.getY() + this.button.localToScene(0, 0).getY() + this.button.getScene().getY() + this.button.getHeight();
            // 显示弹窗
            this.popup.show(this.getParent(), x, y);
            // 更新日期
            this.select.updateDataCalendar(true);
        }
    }

    /**
     * 隐藏弹窗
     */
    public void hidePopup() {
        if (this.popup != null && this.popup.isShowing()) {
            this.popup.hide();
        }
    }

    @Override
    public String getUserAgentStylesheet() {
        return ResourceUtil.toExternalUrl("/fx-plus/css/date-time-picker.css");
    }

    /**
     * @author oyzh
     * @since 2023/12/24
     */
    private class DateTimePickerSelect extends VBox {

        /**
         * 日历对象
         */
        private Calendar calendar;

        /**
         * 当前时间
         */
        private LocalDateTime currDateTime;

        /**
         * 日期数据组件
         */
        private final FlowPane daysPane;

        private final Label labelYear;

        private final Label labelMouth;

        private final ComboBox<String> hour;

        private final ComboBox<String> minute;

        private final ComboBox<String> second;

        private final Button buttonCancel;

        private final Button buttonOK;

        private final Button buttonReset;

        public DateTimePickerSelect() {
            this.setId("dt_time_picker_select");
            this.getStylesheets().add(ResourceUtil.toExternalUrl("/fx-plus/css/date-time-picker.css"));

            // 顶部操作栏
            HBox topAction = new HBox();
            topAction.setSpacing(10);
            topAction.setAlignment(Pos.CENTER);

            // 上一年
            Button preYear = new Button("<");
            preYear.setMaxHeight(25);
            preYear.setPrefHeight(25);
            preYear.setOnAction(this::prevYear);
            preYear.setStyle("-fx-text-fill: black;-fx-background-color: #6cc;-fx-cursor: HAND;-fx-font-size: 10");

            // 上一月
            Button prevMonth = new Button("←");
            preYear.setMaxHeight(25);
            preYear.setPrefHeight(25);
            prevMonth.setOnAction(this::prevMonth);
            prevMonth.setStyle("-fx-text-fill: black;-fx-background-color: #c66;-fx-cursor: HAND;-fx-font-size: 10");

            // 年
            this.labelYear = new FlexLabel();
            this.labelYear.setId("fontStyle");
            this.labelYear.setMaxHeight(25);
            this.labelYear.setMinHeight(25);

            // 月
            this.labelMouth = new FlexLabel();
            this.labelMouth.setId("fontStyle");
            this.labelMouth.setMaxHeight(25);
            this.labelMouth.setMinHeight(25);

            // 下一月
            Button nextMonth = new Button("→");
            preYear.setMaxHeight(25);
            preYear.setPrefHeight(25);
            nextMonth.setOnAction(this::nextMonth);
            nextMonth.setStyle("-fx-text-fill: black;-fx-background-color: #c66;-fx-cursor: HAND;-fx-font-size: 10");

            // 下一年
            Button nextYear = new Button(">");
            preYear.setMaxHeight(25);
            preYear.setPrefHeight(25);
            nextYear.setOnAction(this::nextYear);
            nextYear.setStyle("-fx-text-fill: black;-fx-background-color: #6cc;-fx-cursor: HAND;-fx-font-size: 10");

            // 添加子节点
            topAction.getChildren().add(preYear);
            topAction.getChildren().add(prevMonth);
            topAction.getChildren().add(this.labelYear);
            topAction.getChildren().add(this.labelMouth);
            topAction.getChildren().add(nextMonth);
            topAction.getChildren().add(nextYear);

            // 日期组件
            FlowPane dayPane = new FlowPane();
            dayPane.setAlignment(Pos.CENTER);
            preYear.setMaxHeight(25);
            preYear.setPrefHeight(25);
            dayPane.setPrefWrapLength(280);

            // 星期一
            FlexLabel labelDay1 = new FlexLabel("一");
            labelDay1.setId("dt_day_label");

            // 星期二
            FlexLabel labelDay2 = new FlexLabel("二");
            labelDay2.setId("dt_day_label");

            // 星期三
            FlexLabel labelDay3 = new FlexLabel("三");
            labelDay3.setId("dt_day_label");

            // 星期四
            FlexLabel labelDay4 = new FlexLabel("四");
            labelDay4.setId("dt_day_label");

            // 星期五
            FlexLabel labelDay5 = new FlexLabel("五");
            labelDay5.setId("dt_day_label");

            // 星期六
            FlexLabel labelDay6 = new FlexLabel("六");
            labelDay6.setId("dt_day_label");

            // 星期日
            FlexLabel labelDay7 = new FlexLabel("日");
            labelDay7.setId("dt_day_label");

            // 添加子节点
            dayPane.getChildren().add(labelDay1);
            dayPane.getChildren().add(labelDay2);
            dayPane.getChildren().add(labelDay3);
            dayPane.getChildren().add(labelDay4);
            dayPane.getChildren().add(labelDay5);
            dayPane.getChildren().add(labelDay6);
            dayPane.getChildren().add(labelDay7);

            // 日期数据组件
            this.daysPane = new FlowPane();
            this.daysPane.setAlignment(Pos.CENTER);
            this.daysPane.setPrefWrapLength(280);

            // 时间组件
            HBox timeAction = new HBox();
            timeAction.setAlignment(Pos.CENTER);
            timeAction.setPrefHeight(30);

            // 小时下拉框
            this.hour = new ComboBox<>();
            this.hour.setId("fontStyle");

            // 小时文本框
            Label labelHour = new Label("时");
            labelHour.setId("fontStyle");

            // 分钟下拉框
            this.minute = new ComboBox<>();
            this.minute.setId("fontStyle");

            // 分钟文本框
            Label labelMinute = new Label("分");
            labelMinute.setId("fontStyle");

            // 秒下拉框
            this.second = new ComboBox<>();
            this.second.setId("fontStyle");

            // 秒文本框
            Label labelSecond = new Label("秒");
            labelSecond.setId("fontStyle");

            timeAction.getChildren().add(this.hour);
            timeAction.getChildren().add(labelHour);
            timeAction.getChildren().add(this.minute);
            timeAction.getChildren().add(labelMinute);
            timeAction.getChildren().add(this.second);
            timeAction.getChildren().add(labelSecond);

            HBox bottomAction = new HBox();
            bottomAction.setAlignment(Pos.BASELINE_CENTER);

            // 清除
            this.buttonReset = new Button("清除");
            this.buttonReset.setOnAction(this::buttonResetOnAction);
            this.buttonReset.setOnMousePressed(this::buttonResetOnMousePressed);
            this.buttonReset.setOnMouseReleased(this::buttonResetOnMouseReleased);
            this.buttonReset.setMaxWidth(58.6);
            this.buttonReset.setMaxHeight(25);
            this.buttonReset.setStyle("-fx-background-color:#96e561;-fx-font-size: 12;-fx-cursor: HAND;");

            // 取消
            this.buttonCancel = new Button("取消");
            this.buttonCancel.setOnAction(this::buttonCancelOnAction);
            this.buttonCancel.setOnMousePressed(this::buttonCancelOnMousePressed);
            this.buttonCancel.setOnMouseReleased(this::buttonCancelOnMouseReleased);
            this.buttonCancel.setMaxWidth(58.6);
            this.buttonCancel.setMaxHeight(25);
            this.buttonCancel.setStyle("-fx-background-color:#FFD2D2;-fx-font-size: 12;-fx-cursor: HAND;");

            // 当前时间
            Button buttonNow = new Button("当前");
            buttonNow.setOnAction(this::buttonNowOnAction);
            buttonNow.setOnMousePressed(this::buttonNowOnMousePressed);
            buttonNow.setOnMouseReleased(this::buttonNowOnMouseReleased);
            this.buttonCancel.setMaxWidth(58.6);
            this.buttonCancel.setMaxHeight(25);
            buttonNow.setStyle("-fx-background-color:#FFD2D2;-fx-font-size: 12;-fx-cursor: HAND;");

            // 确定
            this.buttonOK = new Button("确定");
            this.buttonOK.setOnAction(this::buttonOKOnAction);
            this.buttonOK.setOnMousePressed(this::buttonOKOnMousePressed);
            this.buttonOK.setOnMouseReleased(this::buttonOKOnMouseReleased);
            this.buttonOK.setMaxWidth(58.6);
            this.buttonOK.setMaxHeight(25);
            this.buttonOK.setStyle("-fx-background-color:#ACD6FF;-fx-font-size: 12;-fx-cursor: HAND;");

            bottomAction.getChildren().add(this.buttonReset);
            bottomAction.getChildren().add(this.buttonCancel);
            bottomAction.getChildren().add(buttonNow);
            bottomAction.getChildren().add(this.buttonOK);


            this.getChildren().add(topAction);
            this.getChildren().add(dayPane);
            this.getChildren().add(this.daysPane);
            this.getChildren().add(timeAction);
            this.getChildren().add(bottomAction);

            if (getValue() != null) {
                this.currDateTime = getValue();
            }
            this.updateDataCalendar(true);
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
         * 更新日期组件
         *
         * @param open 是否展开
         */
        public void updateDataCalendar(boolean open) {
            this.daysPane.getChildren().clear();
            this.currDateTime = getValue();
            int nowYear = this.currDateTime != null ? this.currDateTime.getYear() : -1;
            int nowMonth = this.currDateTime != null ? this.currDateTime.getMonthValue() : -1;
            Calendar tmpCalendar;
            if (open) {
                this.calendar = tmpCalendar = GregorianCalendar.from(ZonedDateTime.of(this.currDateTime != null ? this.currDateTime : LocalDateTime.now(), ZoneId.systemDefault()));
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
            if (weekMouthFirstDay == 1) {
                for (int i = lastMouthDays - 5; i <= lastMouthDays; i++) {
                    Button btn = new Button(strValue(i));
                    setDisable(btn);
                    this.daysPane.getChildren().add(btn);
                }
                for (int i = 1; i <= mouthDays; i++) {
                    Button btn = new Button(strValue(i));
                    setAble(btn);
                    this.daysPane.getChildren().add(btn);
                }
            } else if (weekMouthFirstDay == 2) {
                for (int i = 1; i <= mouthDays; i++) {
                    Button btn = new Button(strValue(i));
                    setAble(btn);
                    if (this.currDateTime != null && this.currDateTime.getDayOfMonth() == i && this.calendar != null &&
                            this.calendar.get(Calendar.YEAR) == nowYear && this.calendar.get(Calendar.MONTH) + 1 == nowMonth) {
                        btn.getStyleClass().add("dt_day_select");
                        this.calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), this.currDateTime.getDayOfMonth());
                    }
                    this.daysPane.getChildren().add(btn);
                }
            } else {
                for (int i = lastMouthDays - weekMouthFirstDay + 3; i <= lastMouthDays; i++) {
                    Button btn = new Button(strValue(i));
                    setDisable(btn);
                    this.daysPane.getChildren().add(btn);
                }
                for (int i = 1; i <= mouthDays; i++) {
                    Button btn = new Button(strValue(i));
                    setAble(btn);
                    if (this.currDateTime != null && this.currDateTime.getDayOfMonth() == i && this.calendar != null &&
                            this.calendar.get(Calendar.YEAR) == nowYear && this.calendar.get(Calendar.MONTH) + 1 == nowMonth) {
                        btn.getStyleClass().add("dt_day_select");
                        this.calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), this.currDateTime.getDayOfMonth());
                    }
                    this.daysPane.getChildren().add(btn);
                }
            }
            if (weekMouthLastDay != 1) {
                for (int i = 1; i <= 8 - weekMouthLastDay; i++) {
                    Button btn = new Button(strValue(i));
                    setDisable(btn);
                    this.daysPane.getChildren().add(btn);
                }
            }
            this.setTime();
            if (this.calendar != null) {
                this.labelMouth.setText(this.calendar.get(Calendar.MONTH) + 1 + "月");
                this.labelYear.setText(this.calendar.get(Calendar.YEAR) + "年");
            }
        }

        public void setDisable(Button btn) {
            btn.setDisable(true);
            btn.setId("dt_day_disable");
        }

        public void setAble(Button btn) {
            btn.setId("dt_day_enable");
            btn.setOnAction(event -> {
                for (Node node : this.daysPane.getChildren()) {
                    if (Objects.equals(node.getId(), "dt_day_select")) {
                        node.setId("dt_day_enable");
                        break;
                    }
                }
                btn.setId("dt_day_select");
                calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), Integer.valueOf(btn.getText()),
                        calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));
            });
        }

        private void prevYear(ActionEvent event) {
            calendar.add(Calendar.YEAR, -1);
            updateDataCalendar(false);
        }

        private void prevMonth(ActionEvent event) {
            calendar.add(Calendar.MONTH, -1);
            updateDataCalendar(false);
        }

        private void nextYear(ActionEvent event) {
            calendar.add(Calendar.YEAR, 1);
            updateDataCalendar(false);
        }

        private void nextMonth(ActionEvent event) {
            calendar.add(Calendar.MONTH, 1);
            updateDataCalendar(false);
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
                calendar.set(
                        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),
                        Integer.parseInt(hour.getSelectionModel().getSelectedItem()),
                        Integer.parseInt(minute.getSelectionModel().getSelectedItem()),
                        Integer.parseInt(second.getSelectionModel().getSelectedItem())
                );
                LocalDateTime localDateTime = LocalDateTime.ofInstant(calendar.toInstant(), ZoneId.systemDefault());
                setValue(localDateTime);
            }
            hidePopup();
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
            }
            setValue(localDateTime);
            hidePopup();
        }

        protected void buttonCancelOnMousePressed(MouseEvent event) {
            buttonCancel.setStyle("-fx-background-color:#FFFFB9;-fx-font-size: 12;-fx-cursor: HAND;");
        }

        protected void buttonCancelOnMouseReleased(MouseEvent event) {
            buttonCancel.setStyle("-fx-background-color:#FFD2D2;-fx-font-size: 12;-fx-cursor: HAND;");
        }

        protected void buttonCancelOnAction(ActionEvent event) {
            hidePopup();
        }

        protected void buttonResetOnMousePressed(MouseEvent event) {
            buttonReset.setStyle("-fx-background-color:#FFFFB9;-fx-font-size: 12;-fx-cursor: HAND;");
        }

        protected void buttonResetOnMouseReleased(MouseEvent event) {
            buttonReset.setStyle("-fx-background-color:#96e561;-fx-font-size: 12;-fx-cursor: HAND;");
        }

        protected void buttonResetOnAction(ActionEvent event) {
            for (Node node : this.daysPane.getChildren()) {
                if (Objects.equals(node.getId(), "dt_day_select")) {
                    node.setId("dt_day_enable");
                    break;
                }
            }
            this.currDateTime = null;
            clearValue();
        }
    }
}
