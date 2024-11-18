package cn.oyzh.fx.gui.skin;

import cn.oyzh.fx.gui.svg.glyph.CloseSVGGlyph;
import cn.oyzh.i18n.I18nHelper;
import cn.oyzh.fx.plus.skin.ActionTextFieldSkinExt;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

/**
 * 可清除文本输入框皮肤
 *
 * @author oyzh
 * @since 2023/10/9
 */
public class ClearableTextFieldSkin extends ActionTextFieldSkinExt {

    @Override
    protected void updateButtonVisibility() {
        boolean visible = this.getSkinnable().isVisible();
        boolean disable = this.getSkinnable().isDisable();
        boolean hasFocus = this.getSkinnable().isFocused();
        boolean isEmpty = this.getSkinnable().getText() == null || this.getSkinnable().getText().isEmpty();
        boolean shouldBeVisible = !disable && visible && hasFocus && !isEmpty;
        this.button.setVisible(shouldBeVisible);
    }

    public ClearableTextFieldSkin(TextField textField) {
        super(textField, new CloseSVGGlyph("10"));
        this.button.disappear();
        this.button.setTipText(I18nHelper.clear());
    }

    @Override
    protected void onButtonClicked(MouseEvent e) {
        this.setText("");
    }

    @Override
    protected double getButtonSizeMax() {
        return 12;
    }
}
