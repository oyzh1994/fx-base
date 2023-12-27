package cn.oyzh.fx.plus.controls.digital;


/**
 * flot文本域
 *
 * @author oyzh
 * @since 2023/12/22
 */
public class FloatTextField extends DecimalTextField {

    {
        super.setMin((double) Float.MIN_VALUE);
        super.setMax((double) Float.MAX_VALUE);
    }
}
