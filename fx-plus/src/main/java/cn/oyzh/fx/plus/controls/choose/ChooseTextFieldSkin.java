package cn.oyzh.fx.plus.controls.choose;

import cn.oyzh.fx.common.util.NumUtil;
import cn.oyzh.fx.plus.controls.svg.ChooseSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;
import cn.oyzh.fx.plus.skin.ActionTextFieldSkinExt;
import cn.oyzh.fx.plus.skin.TextFieldSkinExt;
import cn.oyzh.fx.plus.theme.ThemeManager;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.Setter;

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
