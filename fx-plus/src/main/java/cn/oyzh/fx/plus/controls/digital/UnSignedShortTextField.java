package cn.oyzh.fx.plus.controls.digital;

/**
 * short文本域
 *
 * @author oyzh
 * @since 2023/12/27
 */
public class UnSignedShortTextField extends ShortTextField {

    {
        super.setMin((long) Short.MIN_VALUE);
        super.setMax((long) Short.MAX_VALUE);
    }
}
