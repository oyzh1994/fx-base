package cn.oyzh.fx.plus.ext;

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
