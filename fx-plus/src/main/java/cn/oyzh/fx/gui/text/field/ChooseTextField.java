package cn.oyzh.fx.gui.text.field;

import cn.oyzh.fx.gui.skin.ChooseTextFieldSkin;

/**
 * 选择输入框
 *
 * @author oyzh
 * @since 2024/07/04
 */
public class ChooseTextField extends LimitTextField {

    public void setAction(Runnable action) {
        this.skin().setAction(action);
    }

    @Override
    public ChooseTextFieldSkin skin() {
        return (ChooseTextFieldSkin) super.skin();
    }

    @Override
    protected ChooseTextFieldSkin createDefaultSkin() {
        return new ChooseTextFieldSkin(this);
    }

    @Override
    public void initNode() {
        this.setEditable(false);
        super.initNode();
    }
}
