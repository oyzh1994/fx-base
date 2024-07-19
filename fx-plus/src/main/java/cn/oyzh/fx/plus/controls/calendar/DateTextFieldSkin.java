package cn.oyzh.fx.plus.controls.calendar;

import atlantafx.base.controls.Calendar;
import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.common.util.NumUtil;
import cn.oyzh.fx.plus.controls.box.FlexHBox;
import cn.oyzh.fx.plus.controls.box.FlexVBox;
import cn.oyzh.fx.plus.controls.svg.CancelSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SubmitSVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;
import cn.oyzh.fx.plus.skin.TextFieldSkinExt;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.window.PopupExt;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
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

/**
 * 日期输入框皮肤
 *
 * @author oyzh
 * @since 2024/07/19
 */
public class DateTextFieldSkin extends TextFieldSkinExt {

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
            this.formatter = DateTimeFormatter.ofPattern("yyy-MM-dd");
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
        // 初始化时间
        LocalDateTime dateTime = this.getLocalDateTime();
        if (dateTime == null) {
            dateTime = LocalDateTime.now();
        }
        calendar.setValue(dateTime.toLocalDate());

        // 按钮组件
        SubmitSVGGlyph submit = new SubmitSVGGlyph();
        submit.setOnMousePrimaryClicked(mouseEvent -> {
            LocalDate date = calendar.getValue();
            if (date == null) {
                this.setText("");
            } else {
                this.setText(this.formatter().format(date));
            }
            this.handleHide();
        });
        submit.setSizeStr("13,11");
        CancelSVGGlyph cancel = new CancelSVGGlyph();
        cancel.setSizeStr("11");
        cancel.setOnMousePrimaryClicked(mouseEvent -> this.handleHide());
        // 按钮组件
        FlexHBox hBox = new FlexHBox(submit, cancel);
        HBox.setMargin(submit, new Insets(5, 0, 0, 3));
        HBox.setMargin(cancel, new Insets(5, 0, 0, 15));
        // 布局组件
        FlexVBox vBox = new FlexVBox();
        vBox.addChild(calendar);
        vBox.addChild(hBox);
        // 初始化弹窗
        if (this.popup == null) {
            this.popup = new PopupExt();
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

    protected LocalDateTime getLocalDateTime() {
        if (StrUtil.isNotBlank(this.getText())) {
            try {
                return LocalDateTime.parse(this.getText(), this.formatter());
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

    public DateTextFieldSkin(TextField textField) {
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
