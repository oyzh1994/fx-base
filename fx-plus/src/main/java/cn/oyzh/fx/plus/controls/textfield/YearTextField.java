package cn.oyzh.fx.plus.controls.textfield;

/**
 * bit文本域
 *
 * @author oyzh
 * @since 2023/12/22
 */
public class YearTextField extends NumberTextField {

    {
        super.setMax(2155L);
        super.setMin(1901L);
    }
}
