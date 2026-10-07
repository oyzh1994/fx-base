package cn.oyzh.fx.gui.skin;

import atlantafx.base.controls.Calendar;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.gui.svg.glyph.CancelSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.DateSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.SubmitSVGGlyph;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.window.PopupExt;
import cn.oyzh.i18n.I18nHelper;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 日期输入框皮肤
 *
 * @author oyzh
 * @since 2024/07/19
 */
public class DateTextFieldSkin extends ActionTextFieldSkin {

    /**
     * 日期格式化器
     */
    private DateTimeFormatter formatter;

    /**
     * 获取日期格式化器
     *
     * @return 日期格式化器
     */
    public DateTimeFormatter getFormatter() {
        return formatter;
    }

    /**
     * 设置日期格式化器
     *
     * @param formatter 日期格式化器
     */
    public void setFormatter(DateTimeFormatter formatter) {
        this.formatter = formatter;
    }

    /**
     * 弹窗
     */
    private PopupExt popup;

    /**
     * 获取格式化器，未设置时使用默认格式 yy-MM-dd
     *
     * @return 日期格式化器
     */
    protected DateTimeFormatter formatter() {
        if (this.formatter == null) {
            this.formatter = DateTimeFormatter.ofPattern("yyy-MM-dd");
        }
        return this.formatter;
    }

    @Override
    protected SVGGlyph getButton() {
        if (this.button == null) {
            this.button = new DateSVGGlyph();
            super.initButton(this.button);
        }
        return this.button;
    }

    @Override
    protected void onButtonClick(MouseEvent e) {
        // 文本输入框
        TextField textField = this.getSkinnable();
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
//        submit.setSizeStr("13,11");
        CancelSVGGlyph cancel = new CancelSVGGlyph();
//        cancel.setSizeStr("11");
        cancel.setOnMousePrimaryClicked(mouseEvent -> this.handleHide());
        // 按钮组件
        FXHBox hBox = new FXHBox(submit, cancel);
        HBox.setMargin(submit, new Insets(5, 0, 0, 3));
        HBox.setMargin(cancel, new Insets(5, 0, 0, 15));
        // 布局组件
        FXVBox vBox = new FXVBox();
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

    /**
     * 解析当前文本为日期时间
     *
     * @return 日期时间，解析失败返回 null
     */
    protected LocalDateTime getLocalDateTime() {
        if (StringUtil.isNotBlank(this.getText())) {
            try {
                return LocalDateTime.parse(this.getText(), this.formatter());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 隐藏弹窗并恢复控件状态
     */
    protected void handleHide() {
        this.popup.hide();
        this.getSkinnable().setDisable(false);
        this.resetButtonColor();
    }

    /**
     * 构造日期文本字段皮肤对象。
     *
     * @param textField 文本框
     */
    public DateTextFieldSkin(TextField textField) {
        super(textField);
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
