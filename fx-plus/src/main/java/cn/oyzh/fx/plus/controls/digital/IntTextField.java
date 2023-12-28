package cn.oyzh.fx.plus.controls.digital;

/**
 * integer文本域
 *
 * @author oyzh
 * @since 2023/12/22
 */
public class IntTextField extends NumberTextField {

    {
        super.setMin((long) Integer.MIN_VALUE);
        super.setMax((long) Integer.MAX_VALUE);
    }
}
