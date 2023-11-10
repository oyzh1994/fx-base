package cn.oyzh.fx.plus.controls.textfield;

import cn.oyzh.fx.plus.skin.textfield.ClearableTextFieldSkin;

/**
 * 可清除文本域
 *
 * @author oyzh
 * @since 2023/08/15
 */
public class ClearableTextField extends LimitTextField {

    {
        this.setSkin(new ClearableTextFieldSkin(this));
    }
}
