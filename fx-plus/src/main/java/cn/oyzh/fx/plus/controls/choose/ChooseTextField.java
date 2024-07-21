package cn.oyzh.fx.plus.controls.choose;

import cn.oyzh.fx.plus.controls.textfield.LimitTextField;

/**
 * 选择输入框
 *
 * @author oyzh
 * @since 2024/07/04
 */
public class ChooseTextField extends LimitTextField {

    {
        this.setEditable(false);
        this.setSkin(new ChooseTextFieldSkin(this));
    }

    /**
     * 当前皮肤
     *
     * @return 皮肤
     */
    public ChooseTextFieldSkin skin() {
        return (ChooseTextFieldSkin) this.getSkin();
    }

    public void setAction(Runnable action) {
        this.skin().setAction(action);
    }
}
