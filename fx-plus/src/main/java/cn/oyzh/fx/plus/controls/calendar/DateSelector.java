package cn.oyzh.fx.plus.controls.calendar;

import atlantafx.base.controls.Calendar;
import cn.hutool.core.date.LocalDateTimeUtil;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author oyzh
 * @since 2023/12/26
 */
public class DateSelector extends CalendarSelector {

    /**
     * 日历组件
     */
    private Calendar calendar;

    public DateSelector(CalendarPicker<?> picker) {
        super(picker);
    }

    @Override
    protected void initNode() {
        // 日期组件
        this.calendar = new Calendar();
        // 设置鼠标
        this.calendar.setCursor(Cursor.HAND);
        // 设置边距
        VBox.setMargin(calendar, new Insets(5, 0, 5, 0));
        // 添加子节点
        this.addChild(this.calendar);
        // 调用父类
        super.initNode();
    }

    @Override
    protected void initListener() {
        super.initListener();
        // 初始化事件
        this.calendar.valueProperty().addListener((observable, oldValue, newValue) -> this.updateTime());
    }

    @Override
    protected void updateTime() {
        LocalDate localDate = this.calendar.getValue();
        // 没有值
        if (localDate == null) {
            this.picker.setValue((LocalDateTime) null);
        } else {// 更新值
            this.picker.setValue(LocalDateTimeUtil.of(localDate));
        }
    }

    @Override
    protected void onClearAction(ActionEvent event) {
        this.calendar.setValue(null);
    }

    @Override
    protected void onNowAction(ActionEvent event) {
        this.calendar.setValue(LocalDateTime.now().toLocalDate());
    }
}
