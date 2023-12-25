package cn.oyzh.fx.plus.controls.date;

import atlantafx.base.controls.Popover;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.oyzh.fx.plus.controls.FXHBox;
import cn.oyzh.fx.plus.controls.FXVBox;
import cn.oyzh.fx.plus.controls.FlexHBox;
import cn.oyzh.fx.plus.controls.button.FXButton;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.fx.plus.controls.text.FXLabel;
import cn.oyzh.fx.plus.controls.textfield.ClearableTextField;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * @author oyzh
 * @since 2023/12/24
 */
public class DateTimePicker extends FlexHBox {

    /**
     * 格式化器
     */
    private final DateTimeFormatter formatter;

    /**
     * 值属性
     */
    private ObjectProperty<LocalDateTime> valueProperty;

    /**
     * 弹出组件
     */
    private Popover popup;

    /**
     * 选择器
     */
    private DateTimePickerSelector selector;

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
                this.popup = new Popover();
                this.selector = new DateTimePickerSelector();
                this.popup.setContentNode(this.selector);
            }
            // 更新日期
            this.selector.updateCalendar();
            // 显示弹窗
            this.popup.show(this);
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

    /**
     * @author oyzh
     * @since 2023/12/24
     */
    private class DateTimePickerSelector extends FXVBox {

        /**
         * 日历对象
         */
        private Calendar calendar;

        /**
         * 小时下拉框
         */
        private final FXComboBox<String> hour;

        /**
         * 分钟下拉框
         */
        private final FXComboBox<String> minute;

        /**
         * 秒下拉框
         */
        private final FXComboBox<String> second;

        /**
         * 日历组件
         */
        private final atlantafx.base.controls.Calendar calendarPane;

        public DateTimePickerSelector() {
            // 日期组件
            this.calendarPane = new atlantafx.base.controls.Calendar();
            this.calendarPane.valueProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue == null) {
                    setValue(null);
                } else {
                    setValue(LocalDateTimeUtil.of(newValue));
                }
            });

            // 时间组件
            FXHBox timeAction = new FXHBox();
            timeAction.setAlignment(Pos.CENTER);
            timeAction.setPrefHeight(30);
            timeAction.setPadding(new Insets(5, 0, 5, 0));

            // 小时下拉框
            this.hour = new FXComboBox<>();
            this.hour.setRealWidth(75);

            // 小时文本框
            FXLabel labelHour = new FXLabel("时");

            // 分钟下拉框
            this.minute = new FXComboBox<>();
            this.minute.setRealWidth(75);

            // 分钟文本框
            FXLabel labelMinute = new FXLabel("分");

            // 秒下拉框
            this.second = new FXComboBox<>();
            this.second.setRealWidth(75);

            // 秒文本框
            FXLabel labelSecond = new FXLabel("秒");

            HBox.setMargin(labelHour, new Insets(0,5,0,5));
            HBox.setMargin(labelMinute, new Insets(0,5,0,5));
            HBox.setMargin(labelSecond, new Insets(0,0,0,5));

            timeAction.getChildren().add(this.hour);
            timeAction.getChildren().add(labelHour);
            timeAction.getChildren().add(this.minute);
            timeAction.getChildren().add(labelMinute);
            timeAction.getChildren().add(this.second);
            timeAction.getChildren().add(labelSecond);

            FXHBox bottomAction = new FXHBox();
            bottomAction.setAlignment(Pos.BASELINE_CENTER);

            // 清除
            FXButton buttonClear = new FXButton("清除");
            buttonClear.getStyleClass().add("danger");
            buttonClear.setOnAction(event -> this.calendarPane.setValue(null));
            buttonClear.setRealWidth(60);
            buttonClear.setRealHeight(25);

            // 当前
            FXButton buttonNow = new FXButton("当前");
            buttonNow.getStyleClass().add("success");
            buttonNow.setOnAction(event -> this.calendarPane.setValue(LocalDateTime.now().toLocalDate()));
            buttonNow.setRealWidth(60);
            buttonNow.setRealHeight(25);

            // 关闭
            FXButton buttonClose = new FXButton("关闭");
            buttonClose.getStyleClass().add("default");
            buttonClose.setOnAction(event -> hidePopup());
            buttonClose.setRealWidth(60);
            buttonClose.setRealHeight(25);


            HBox.setMargin(buttonClear, new Insets(0, 5, 0, 0));
            HBox.setMargin(buttonNow, new Insets(0, 5, 0, 0));

            bottomAction.getChildren().add(buttonClear);
            bottomAction.getChildren().add(buttonNow);
            bottomAction.getChildren().add(buttonClose);

            this.getChildren().add(this.calendarPane);
            this.getChildren().add(timeAction);
            this.getChildren().add(bottomAction);
        }

        /**
         * 更新日期组件
         */
        public void updateCalendar() {
            LocalDateTime time = getValue();
            this.calendar = GregorianCalendar.from(ZonedDateTime.of(time != null ? time : LocalDateTime.now(), ZoneId.systemDefault()));
            int mouthDays = this.calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            this.calendar.set(this.calendar.get(Calendar.YEAR), this.calendar.get(Calendar.MONTH), mouthDays, this.calendar.get(Calendar.HOUR_OF_DAY), this.calendar.get(Calendar.MINUTE), this.calendar.get(Calendar.SECOND));
            this.calendar.set(this.calendar.get(Calendar.YEAR), this.calendar.get(Calendar.MONTH), 1, this.calendar.get(Calendar.HOUR_OF_DAY), this.calendar.get(Calendar.MINUTE), this.calendar.get(Calendar.SECOND));
            this.calendar.add(Calendar.MONTH, -1);
            this.calendar.add(Calendar.MONTH, 1);
            this.initTime();
        }

        /**
         * 初始化时间组件
         */
        private void initTime() {
            for (int i = 0; i < 24; i++) {
                this.hour.getItems().add(i < 10 ? "0" + i : i + "");
            }
            for (int i = 0; i < 60; i++) {
                this.minute.getItems().add(i < 10 ? "0" + i : i + "");
            }
            for (int i = 0; i < 60; i++) {
                this.second.getItems().add(i < 10 ? "0" + i : i + "");
            }
            this.hour.getSelectionModel().select(calendar.get(Calendar.HOUR_OF_DAY));
            this.minute.getSelectionModel().select(calendar.get(Calendar.MINUTE));
            this.second.getSelectionModel().select(calendar.get(Calendar.SECOND));
        }
    }
}
