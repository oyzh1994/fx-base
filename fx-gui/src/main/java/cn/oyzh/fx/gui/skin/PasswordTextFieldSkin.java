package cn.oyzh.fx.gui.skin;

import cn.oyzh.fx.gui.svg.glyph.ChooseSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.EyeSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.ShowSVGGlyph;
import cn.oyzh.fx.gui.text.field.PasswordTextField;
import cn.oyzh.fx.plus.information.TooltipExt;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.TextFieldSkin;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Window;

/**
 * 文本域皮肤扩展
 *
 * @author oyzh
 * @since 2023/10/25
 */
public class PasswordTextFieldSkin extends ActionTextFieldSkin {

    public PasswordTextFieldSkin(PasswordTextField textField) {
        super(textField, new EyeSVGGlyph());
        this.button.disappear();
        this.button.setTipText(I18nHelper.showPassword());
    }

    @Override
    protected void onButtonClicked(MouseEvent e) {
        PasswordTextField textField = (PasswordTextField) getSkinnable();
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
