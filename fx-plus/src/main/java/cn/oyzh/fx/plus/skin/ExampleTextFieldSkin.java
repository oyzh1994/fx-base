package cn.oyzh.fx.plus.skin;

import cn.oyzh.fx.plus.controls.svg.ExampleSVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import lombok.Getter;
import lombok.Setter;

/**
 * 示例文本输入框皮肤
 *
 * @author oyzh
 * @since 2024/07/04
 */
public class ExampleTextFieldSkin extends ActionTextFieldSkinExt {

    /**
     * 示例文本
     */
    @Getter
    @Setter
    protected String exampleText;

    protected void onButtonClicked(MouseEvent e) {
        if (this.exampleText != null) {
            this.setText(this.exampleText);
        }
    }

    public ExampleTextFieldSkin(TextField textField) {
        super(textField,new ExampleSVGGlyph("13"));
        this.button.setTipText(I18nHelper.example());
    }
}
