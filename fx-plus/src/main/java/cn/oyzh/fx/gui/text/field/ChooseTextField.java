package cn.oyzh.fx.gui.text.field;

import cn.oyzh.fx.gui.skin.ChooseTextFieldSkin;
import javafx.scene.control.Skin;

/**
 * 选择输入框
 *
 * @author oyzh
 * @since 2024/07/04
 */
public class ChooseTextField extends LimitTextField {

    {
        this.setEditable(false);
    }

    /**
     * 当前皮肤
     *
     * @return 皮肤
     */
    public ChooseTextFieldSkin skin() {
        if (this.getSkin() == null) {
            this.setSkin(this.createDefaultSkin());
        }
        return (ChooseTextFieldSkin) this.getSkin();
    }

    public void setAction(Runnable action) {
        this.skin().setAction(action);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new ChooseTextFieldSkin(this);
    }
}
