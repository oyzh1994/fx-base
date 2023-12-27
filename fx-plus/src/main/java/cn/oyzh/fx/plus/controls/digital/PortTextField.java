package cn.oyzh.fx.plus.controls.digital;

/**
 * @author oyzh
 * @since 2022/12/23
 */
public class PortTextField extends NumberTextField {

    {
        this.setMin(1L);
        this.setMax(65535L);
    }
}
