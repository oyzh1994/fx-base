package cn.oyzh.fx.plus.controls.digital;

/**
 * bit文本域
 *
 * @author oyzh
 * @since 2023/12/22
 */
public class BitTextField extends NumberTextField {

    {
        super.setMax(1L);
        super.setMin(0L);
    }
}
