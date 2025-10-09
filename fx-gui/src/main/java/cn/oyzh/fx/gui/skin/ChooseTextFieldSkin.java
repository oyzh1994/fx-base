package cn.oyzh.fx.gui.skin;

import cn.oyzh.fx.gui.svg.glyph.ChooseSVGGlyph;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.TextField;

/**
 * 选择输入框皮肤
 *
 * @author oyzh
 * @since 2024/07/04
 */
public class ChooseTextFieldSkin extends ActionTextFieldSkin {

    public ChooseTextFieldSkin(TextField textField) {
        super(textField,new ChooseSVGGlyph());
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
