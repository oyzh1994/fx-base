package cn.oyzh.fx.plus.skin;

import cn.oyzh.fx.plus.controls.svg.ChooseSVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;
import javafx.scene.control.TextField;

/**
 * 选择输入框皮肤
 *
 * @author oyzh
 * @since 2024/07/04
 */
public class ChooseTextFieldSkin extends ActionTextFieldSkinExt {

    public ChooseTextFieldSkin(TextField textField) {
        super(textField,new ChooseSVGGlyph("13"));
        this.button.setTipText(I18nHelper.choose());
    }
}
