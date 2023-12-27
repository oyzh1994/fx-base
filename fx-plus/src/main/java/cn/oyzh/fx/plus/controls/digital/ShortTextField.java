package cn.oyzh.fx.plus.controls.digital;

/**
 * short文本域
 *
 * @author oyzh
 * @since 2023/12/27
 */
public class ShortTextField extends NumberTextField {

    {
        super.setMin(Short.MIN_VALUE);
        super.setMax(Short.MAX_VALUE);
    }
}
