package cn.oyzh.fx.gui.skin;

import atlantafx.base.controls.Calendar;
import cn.oyzh.common.date.LocalDateTimeUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.gui.svg.glyph.CancelSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.DateSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.SubmitSVGGlyph;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.box.FlexHBox;
import cn.oyzh.fx.plus.controls.box.FlexVBox;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.window.PopupExt;
import cn.oyzh.i18n.I18nHelper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 日期输入框皮肤
 *
 * @author oyzh
 * @since 2024/07/19
 */
public class DateTimeTextFieldSkin extends ActionTextFieldSkinExt {

    /**
     * 日期格式化器
     */
    @Setter
    @Getter
    private DateTimeFormatter formatter;

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

    @Override
    protected void onButtonClicked(MouseEvent e) {
        // 文本输入框
        TextField textField = getSkinnable();
        textField.setDisable(true);

        // 日期组件
        Calendar calendar = new Calendar();
        calendar.setCursor(Cursor.HAND);
        // 初始化时间
        LocalDateTime dateTime = this.getLocalDateTime();
        if (dateTime == null) {
            dateTime = LocalDateTime.now();
        }
        calendar.setValue(dateTime.toLocalDate());

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
        hour.select(dateTime.getHour());
        minute.select(dateTime.getMinute());
        second.select(dateTime.getSecond());

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
                LocalTime localTime = LocalTime.of(hour.getSelectedIndex(), minute.getSelectedIndex(), second.getSelectedIndex());
                LocalDateTime time = LocalDateTimeUtil.of(date, localTime);
                this.setText(this.formatter().format(time));
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

    protected LocalDateTime getLocalDateTime() {
        String text = this.getText();
        if (StringUtil.isNotBlank(text)) {
            try {
                return LocalDateTime.parse(text, this.formatter());
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
        super(textField, new DateSVGGlyph("13"));
        this.button.disappear();
        this.button.setTipText(I18nHelper.choose());
    }

    @Override
    protected void updateButtonVisibility() {
        boolean visible = this.getSkinnable().isVisible();
        boolean disable = this.getSkinnable().isDisable();
        boolean hasFocus = this.getSkinnable().isFocused();
        boolean shouldBeVisible = !disable && visible && hasFocus;
        this.button.setVisible(shouldBeVisible);
    }
}
