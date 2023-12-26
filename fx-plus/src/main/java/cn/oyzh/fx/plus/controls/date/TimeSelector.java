package cn.oyzh.fx.plus.controls.date;

import cn.oyzh.fx.plus.controls.FXHBox;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.fx.plus.controls.text.FXLabel;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;

import java.time.LocalDateTime;

/**
 * @author oyzh
 * @since 2023/12/24
 */
public class TimeSelector extends CalendarSelector {

    /**
     * 小时下拉框
     */
    private FXComboBox<String> hour;

    /**
     * 分钟下拉框
     */
    private FXComboBox<String> minute;

    /**
     * 秒下拉框
     */
    private FXComboBox<String> second;

    public TimeSelector(CalendarPicker<?> picker) {
        super(picker);
    }

    @Override
    protected void initNode() {
        // 时间组件
        FXHBox timeAction = new FXHBox();
        timeAction.setAlignment(Pos.CENTER);
        timeAction.setRealHeight(40);
        timeAction.setPadding(new Insets(5, 0, 5, 0));

        // 小时下拉框
        this.hour = new FXComboBox<>();
        this.hour.setRealWidth(80);

        // 小时文本框
        FXLabel labelHour = new FXLabel("时");

        // 分钟下拉框
        this.minute = new FXComboBox<>();
        this.minute.setRealWidth(80);

        // 分钟文本框
        FXLabel labelMinute = new FXLabel("分");

        // 秒下拉框
        this.second = new FXComboBox<>();
        this.second.setRealWidth(80);

        // 秒文本框
        FXLabel labelSecond = new FXLabel("秒");

        HBox.setMargin(labelHour, new Insets(0, 5, 0, 5));
        HBox.setMargin(labelMinute, new Insets(0, 5, 0, 5));
        HBox.setMargin(labelSecond, new Insets(0, 0, 0, 5));

        timeAction.getChildren().add(this.hour);
        timeAction.getChildren().add(labelHour);
        timeAction.getChildren().add(this.minute);
        timeAction.getChildren().add(labelMinute);
        timeAction.getChildren().add(this.second);
        timeAction.getChildren().add(labelSecond);

        // 添加子节点
        this.addChild(timeAction);

        // 调用父类
        super.initNode();
    }

    @Override
    protected void initListener() {
        super.initListener();
        // 初始化事件
        this.hour.valueProperty().addListener((observable, oldValue, newValue) -> this.updateTime());
        this.minute.valueProperty().addListener((observable, oldValue, newValue) -> this.updateTime());
        this.second.valueProperty().addListener((observable, oldValue, newValue) -> this.updateTime());
    }

    @Override
    protected void updateCalendar() {
        LocalDateTime time = this.picker.getValue();
        time = time != null ? time : LocalDateTime.now();
        this.hour.select(time.getHour());
        this.minute.select(time.getMinute());
        this.second.select(time.getSecond());
    }

    @Override
    protected void initTime() {
        for (int i = 0; i < 24; i++) {
            this.hour.getItems().add(i < 10 ? "0" + i : i + "");
        }
        for (int i = 0; i < 60; i++) {
            this.minute.getItems().add(i < 10 ? "0" + i : i + "");
        }
        for (int i = 0; i < 60; i++) {
            this.second.getItems().add(i < 10 ? "0" + i : i + "");
        }
    }

    @Override
    protected void updateTime() {
        LocalDateTime dateTime = LocalDateTime.now();
        dateTime = dateTime.withHour(this.hour.getSelectedIndex())
                .withMinute(this.minute.getSelectedIndex())
                .withSecond(this.second.getSelectedIndex());
        this.picker.setValue(dateTime);
    }

    @Override
    protected void onClearAction(ActionEvent event) {
        this.picker.setValue(null);
    }

    @Override
    protected void onNowAction(ActionEvent event) {
        this.picker.setValue(LocalDateTime.now());
    }
}
