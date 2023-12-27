package cn.oyzh.fx.plus.controls.digital;

/**
 * double文本域
 *
 * @author oyzh
 * @since 2023/12/22
 */
public class DoubleTextField extends DecimalTextField {

    {
        super.setMin(Double.MIN_VALUE);
        super.setMax(Double.MAX_VALUE);
    }
}
