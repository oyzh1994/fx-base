package cn.oyzh.fx.plus.controls.calendar;

import atlantafx.base.controls.Calendar;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.common.util.NumUtil;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.box.FlexHBox;
import cn.oyzh.fx.plus.controls.box.FlexVBox;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.fx.plus.controls.svg.CancelSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SubmitSVGGlyph;
import cn.oyzh.fx.plus.controls.text.FXLabel;
import cn.oyzh.fx.plus.i18n.I18nHelper;
import cn.oyzh.fx.plus.skin.TextFieldSkinExt;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.window.PopupExt;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 日期输入框皮肤
 *
 * @author oyzh
 * @since 2024/07/19
 */
public class DateTimeTextFieldSkin extends TextFieldSkinExt {

    /**
     * 日期格式化器
     */
    @Setter
    @Getter
    private DateTimeFormatter formatter;

    /**
     * 日期按钮
     */
    protected final SVGGlyph button;

    /**
     * 弹窗
     */
    private PopupExt popup;

    protected DateTimeFormatter formatter() {
        if (this.formatter == null) {
            this.formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        }
        return this.formatter;
    }

    /**
     * 显示日期弹窗
     */
    public void showDatePopup() {
        // 文本输入框
        TextField textField = getSkinnable();
        textField.setDisable(true);
        // 日期组件
        Calendar calendar = new Calendar();
        calendar.setCursor(Cursor.HAND);
        // 当前事件
        LocalDateTime now = LocalDateTime.now();
        LocalDate localDate = this.getLocalDate();
        if (localDate != null) {
            calendar.setValue(localDate);
        } else {
            calendar.setValue(now.toLocalDate());
        }

        // 小时文本框
        FXLabel labelHour = new FXLabel(I18nHelper.hour());
        // 小时下拉框
        FXComboBox<String> hour = new FXComboBox<>();
        hour.setRealWidth(70);

        // 分钟文本框
        FXLabel labelMinute = new FXLabel(I18nHelper.minute());
        // 分钟下拉框
        FXComboBox<String> minute = new FXComboBox<>();
        minute.setRealWidth(70);

        // 秒文本框
        FXLabel labelSecond = new FXLabel(I18nHelper.seconds());
        // 秒下拉框
        FXComboBox<String> second = new FXComboBox<>();
        second.setRealWidth(70);

        HBox.setMargin(labelHour, new Insets(0, 5, 0, 5));
        HBox.setMargin(labelMinute, new Insets(0, 5, 0, 5));
        HBox.setMargin(labelSecond, new Insets(0, 0, 0, 5));

        for (int i = 0; i < 24; i++) {
            hour.getItems().add(i < 10 ? "0" + i : i + "");
        }
        for (int i = 0; i < 60; i++) {
            minute.getItems().add(i < 10 ? "0" + i : i + "");
        }
        for (int i = 0; i < 60; i++) {
            second.getItems().add(i < 10 ? "0" + i : i + "");
        }

        // 选择时分秒
        hour.select(now.getHour());
        minute.select(now.getMinute());
        second.select(now.getSecond());

        // 时间组件
        FXHBox timeBox = new FXHBox();
        timeBox.setAlignment(Pos.CENTER);
        timeBox.setRealHeight(30);
        timeBox.addChild(hour);
        timeBox.addChild(labelHour);
        timeBox.addChild(minute);
        timeBox.addChild(labelMinute);
        timeBox.addChild(second);
        timeBox.addChild(labelSecond);

        // 按钮组件
        SubmitSVGGlyph submit = new SubmitSVGGlyph();
        submit.setSizeStr("13,11");
        submit.setOnMousePrimaryClicked(mouseEvent -> {
            LocalDate date = calendar.getValue();
            if (date == null) {
                this.setText("");
            } else {
                LocalDateTime dateTime = LocalDateTimeUtil.of(date);
                dateTime = dateTime.withHour(hour.getSelectedIndex()).withMinute(minute.getSelectedIndex()).withSecond(second.getSelectedIndex());
                this.setText(this.formatter().format(dateTime));
            }
            this.handleHide();
        });
        CancelSVGGlyph cancel = new CancelSVGGlyph();
        cancel.setSizeStr("11");
        cancel.setOnMousePrimaryClicked(mouseEvent -> this.handleHide());
        // 按钮组件
        FlexHBox hBox = new FlexHBox(submit, cancel);
        hBox.setRealHeight(30);
        HBox.setMargin(submit, new Insets(5, 0, 0, 3));
        HBox.setMargin(cancel, new Insets(5, 0, 0, 15));

        // 布局组件
        FlexVBox vBox = new FlexVBox();
        vBox.addChild(calendar);
        vBox.addChild(timeBox);
        vBox.addChild(hBox);

        // 监听时间变化
        calendar.valueProperty().addListener((observableValue, localDate1, t1) -> {
            if (t1 == null) {
                timeBox.disable();
            } else {
                timeBox.enable();
            }
        });

        // 初始化弹窗
        if (this.popup == null) {
            this.popup = new PopupExt();
            this.popup.setWidth(300);
            this.popup.setOnHiding(windowEvent -> this.handleHide());
        }
        this.popup.setContentNode(vBox);
        this.popup.showPopup(this.getSkinnable());
    }

    protected LocalDate getLocalDate() {
        if (StrUtil.isNotBlank(this.getText())) {
            try {
                return LocalDate.parse(this.getText(), this.formatter());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    protected void handleHide() {
        this.popup.hide();
        this.getSkinnable().setDisable(false);
        this.resetButtonColor();
    }

    public DateTimeTextFieldSkin(TextField textField) {
        super(textField);
        // 初始化清除按钮
        this.button = new SVGGlyph("/fx-plus/font/date.svg", "14");
        this.button.setEnableWaiting(false);
        this.button.setFocusTraversable(false);
        this.button.setTipText(I18nHelper.choose());
        this.button.setPadding(new Insets(0));
        this.button.setOnMousePrimaryClicked(event -> this.showDatePopup());
        this.button.setOnMouseMoved(mouseEvent -> this.button.setColor("#DC143C"));
        this.button.setOnMouseExited(mouseEvent -> this.resetButtonColor());
        this.getChildren().add(this.button);
    }

    public void resetButtonColor() {
        this.button.setColor(this.getButtonColor());
    }

    @Override
    protected Color getButtonColor() {
        if (!ThemeManager.isDarkMode()) {
            return Color.valueOf("#696969");
        }
        return super.getButtonColor();
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        super.layoutChildren(x, y, w, h);
        // 计算组件大小
        double btnSize = this.snapSizeX(h * 0.7);
        // 限制最大最小值
        btnSize = NumUtil.limit(btnSize, 14, 20);
        // 按钮大小，组件高度
        this.button.setSize(btnSize);
        // 位移的areaX值，规则 组件宽+x-按钮大小
        double areaX = w + x - btnSize - 8;
        // 设置位置
        super.positionInArea(this.button, areaX, y, btnSize, h, 0, HPos.CENTER, VPos.CENTER);
    }
}
