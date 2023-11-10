package cn.oyzh.fx.plus.controls.textfield;

import cn.oyzh.fx.plus.controls.textfield.NumberTextField;

/**
 * @author oyzh
 * @since 2022/12/23
 */
public class PortField extends NumberTextField {

    {
        this.setMin(1L);
        this.setMax(65535L);
    }
}
