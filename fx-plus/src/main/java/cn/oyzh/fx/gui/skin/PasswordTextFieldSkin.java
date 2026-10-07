package cn.oyzh.fx.gui.skin;

import cn.oyzh.fx.gui.svg.glyph.EyeSVGGlyph;
import cn.oyzh.fx.gui.text.field.PasswordTextField;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

/**
 * 密码输入框皮肤，带明文显示切换按钮
 *
 * @author oyzh
 * @since 2025-04-02
 */
public class PasswordTextFieldSkin extends ActionTextFieldSkin {

    /**
     * 构造密码输入框皮肤
     *
     * @param textField 关联的密码输入框
     */
    public PasswordTextFieldSkin(PasswordTextField textField) {
        super(textField);
    }

    @Override
    protected SVGGlyph getButton() {
        if (super.button == null) {
            super.button = new EyeSVGGlyph();
            super.initButton(super.button);
        }
        return super.button;
    }

    @Override
    protected void onButtonClick(MouseEvent e) {
        PasswordTextField textField = (PasswordTextField) this.getSkinnable();
        if (textField.getRevealPassword()) {
            textField.setRevealPassword(false);
            this.button.setTipText(I18nHelper.showPassword());
        } else {
            textField.setRevealPassword(true);
            this.button.setTipText(I18nHelper.showPassword());
        }
    }

    @Override
    protected void updateButtonVisibility() {
        boolean visible = this.getSkinnable().isVisible();
        boolean disable = this.getSkinnable().isDisable();
        boolean hasFocus = this.getSkinnable().isFocused();
        boolean shouldBeVisible = !disable && visible && hasFocus;
        this.button.setVisible(shouldBeVisible);
    }

//    @Override
//    protected void setButtonSize(double size) {
//        this.button.setSize(size * 1.2, size);
//    }
}
