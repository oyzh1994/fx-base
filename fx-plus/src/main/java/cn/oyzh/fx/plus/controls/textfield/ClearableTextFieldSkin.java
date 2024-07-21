package cn.oyzh.fx.plus.controls.textfield;

import cn.oyzh.fx.plus.controls.svg.CloseSVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;
import cn.oyzh.fx.plus.skin.ActionTextFieldSkinExt;
import javafx.scene.control.TextField;

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
        super(textField, new CloseSVGGlyph("12"));
        this.button.setVisible(false);
        this.button.setTipText(I18nHelper.clear());
    }
}
