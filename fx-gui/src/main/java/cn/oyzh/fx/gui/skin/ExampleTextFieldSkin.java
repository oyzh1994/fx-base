package cn.oyzh.fx.gui.skin;

import cn.oyzh.fx.gui.svg.glyph.ExampleSVGGlyph;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

/**
 * 示例文本输入框皮肤
 *
 * @author oyzh
 * @since 2024/07/04
 */
public class ExampleTextFieldSkin extends ActionTextFieldSkin {

    public String getExampleText() {
        return exampleText;
    }

    public void setExampleText(String exampleText) {
        this.exampleText = exampleText;
    }

    /**
     * 示例文本
     */
    protected String exampleText;

    protected void onButtonClicked(MouseEvent e) {
        if (this.exampleText != null) {
            this.setText(this.exampleText);
        }
    }

    public ExampleTextFieldSkin(TextField textField) {
        super(textField,new ExampleSVGGlyph("13"));
        this.button.disappear();
        this.button.setTipText(I18nHelper.example());
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
