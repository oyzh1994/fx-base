package cn.oyzh.fx.gui.textfield;

import cn.oyzh.fx.gui.skin.ClearableTextFieldSkin;
import cn.oyzh.fx.plus.controls.textfield.LimitTextField;
import javafx.scene.control.Skin;

/**
 * 可清除文本域
 *
 * @author oyzh
 * @since 2023/08/15
 */
public class ClearableTextField extends LimitTextField {

    public ClearableTextField( ) {
        super();
    }

    public ClearableTextField(String text) {
        super.setText(text);
    }

    public ClearableTextField(Long maxLen) {
        this.setMaxLen(maxLen);
    }

    public ClearableTextField(String text, Long maxLen) {
        super.setText(text);
        this.setMaxLen(maxLen);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new ClearableTextFieldSkin(this);
    }

}
