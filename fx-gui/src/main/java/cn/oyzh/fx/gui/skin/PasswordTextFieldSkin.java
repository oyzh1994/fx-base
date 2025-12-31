package cn.oyzh.fx.gui.skin;

import cn.oyzh.fx.gui.svg.glyph.EyeSVGGlyph;
import cn.oyzh.fx.gui.text.field.PasswordTextField;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

/**
 * 文本域皮肤扩展
 *
 * @author oyzh
 * @since 2023/10/25
 */
public class PasswordTextFieldSkin extends ActionTextFieldSkin {

    public PasswordTextFieldSkin(PasswordTextField textField) {
        super(textField);
        // super(textField, new EyeSVGGlyph());
        // this.button.disappear();
        // this.button.setTipText(I18nHelper.showPassword());
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
    protected void onButtonClicked(MouseEvent e) {
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

    @Override
    protected void setButtonSize(double size) {
        this.button.setSize(size * 1.2, size);
    }
}
