package cn.oyzh.fx.plus.ext;

import cn.oyzh.fx.plus.controls.BaseTextField;

/**
 * 范围文本域
 *
 * @author oyzh
 * @since 2023/08/15
 */
public class ClearableTextField extends BaseTextField {

    {
        this.setSkin(new ClearableTextFieldSkin(this));
    }
}
