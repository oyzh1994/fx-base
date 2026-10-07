package cn.oyzh.fx.gui.skin;

import cn.oyzh.fx.gui.svg.glyph.ChooseSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.control.TextField;

/**
 * 选择输入框皮肤
 *
 * @author oyzh
 * @since 2024-07-26
 */
public class ChooseTextFieldSkin extends ActionTextFieldSkin {

    @Override
    protected SVGGlyph getButton() {
        if (this.button == null) {
            this.button = new ChooseSVGGlyph();
            super.initButton(this.button);
        }
        return this.button;
    }

    /**
     * 构造选择文本字段皮肤对象。
     *
     * @param textField 文本框
     */
    public ChooseTextFieldSkin(TextField textField) {
        super(textField);
        // super(textField, new ChooseSVGGlyph());
        // this.button.disappear();
        // this.button.setTipText(I18nHelper.choose());
    }
    //
    @Override
    protected void updateButtonVisibility() {
        boolean visible = this.getSkinnable().isVisible();
        boolean disable = this.getSkinnable().isDisable();
        boolean hasFocus = this.getSkinnable().isFocused();
        boolean shouldBeVisible = !disable && visible && hasFocus;
        this.button.setVisible(shouldBeVisible);
    }

    @Override
    protected double getButtonSizeMax() {
        return 12;
    }
}
