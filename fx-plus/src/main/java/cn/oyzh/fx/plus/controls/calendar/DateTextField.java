package cn.oyzh.fx.plus.controls.calendar;

import cn.oyzh.fx.plus.controls.textfield.LimitTextField;

/**
 * @author oyzh
 * @since 2023/12/26
 */
public class DateTextField extends LimitTextField {

    {
        this.setEditable(false);
        this.setSkin(new DateTextFieldSkin(this));
    }

}
