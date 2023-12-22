package cn.oyzh.fx.plus.controls.textfield;

/**
 * bit文本域
 *
 * @author oyzh
 * @since 2023/12/22
 */
public class ByteTextField extends NumberTextField {

    {
        super.setMin((long) Byte.MIN_VALUE);
        super.setMax((long) Byte.MAX_VALUE);
    }
}
