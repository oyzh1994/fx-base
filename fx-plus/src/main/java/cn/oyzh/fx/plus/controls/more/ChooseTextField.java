package cn.oyzh.fx.plus.controls.more;

import cn.oyzh.fx.plus.controls.textfield.LimitTextField;
import cn.oyzh.fx.plus.skin.ChooseTextFieldSkin;

/**
 * 文件选择框
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

    public void setChooseAction(Runnable action) {
        this.skin().setChooseAction(action);
    }
}
