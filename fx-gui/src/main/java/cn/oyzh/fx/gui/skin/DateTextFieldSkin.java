package cn.oyzh.fx.gui.skin;

import atlantafx.base.controls.Calendar;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.gui.svg.glyph.CancelSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.DateSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.SubmitSVGGlyph;
import cn.oyzh.fx.plus.controls.box.FlexHBox;
import cn.oyzh.fx.plus.controls.box.FlexVBox;
import cn.oyzh.fx.plus.skin.ActionTextFieldSkinExt;
import cn.oyzh.fx.plus.window.PopupExt;
import cn.oyzh.i18n.I18nHelper;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
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
public class DateTextFieldSkin extends ActionTextFieldSkinExt {

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
            this.formatter = DateTimeFormatter.ofPattern("yyy-MM-dd");
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

    protected void handleHide() {
        this.popup.hide();
        this.getSkinnable().setDisable(false);
        this.resetButtonColor();
    }

    public DateTextFieldSkin(TextField textField) {
        super(textField,new DateSVGGlyph("13"));
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
