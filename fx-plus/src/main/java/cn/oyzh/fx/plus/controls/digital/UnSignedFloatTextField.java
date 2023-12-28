package cn.oyzh.fx.plus.controls.digital;



/**
 * float文本域
 *
 * @author oyzh
 * @since 2023/12/22
 */
public class UnSignedFloatTextField extends FloatTextField {
    {
        super.setMin(0D);
        super.setMax((double) Float.MAX_VALUE);
    }
}
