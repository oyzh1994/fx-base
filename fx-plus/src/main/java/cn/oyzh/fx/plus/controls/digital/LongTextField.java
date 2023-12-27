package cn.oyzh.fx.plus.controls.digital;

/**
 * long文本域
 *
 * @author oyzh
 * @since 2023/12/22
 */
public class LongTextField extends NumberTextField {

    {
        super.setMin(Long.MIN_VALUE);
        super.setMax(Long.MAX_VALUE);
    }
}
