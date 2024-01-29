package cn.oyzh.fx.plus.controls.digital;

/**
 * bit文本域
 *
 * @author oyzh
 * @since 2023/12/22
 */
public class BitTextField extends NumberTextField {

    public BitTextField() {
        super(false);
        this.setMin(0L);
        this.setMax(2L);
    }
}
