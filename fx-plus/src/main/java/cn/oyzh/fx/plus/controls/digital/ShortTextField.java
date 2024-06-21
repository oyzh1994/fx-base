package cn.oyzh.fx.plus.controls.digital;

/**
 * short文本域
 *
 * @author oyzh
 * @since 2023/12/27
 */
public class ShortTextField extends NumberTextField {

    public ShortTextField() {
        super(false);
    }

    public ShortTextField(boolean unsigned) {
        super(unsigned);
    }

    public ShortTextField(boolean unsigned, Long maxLen) {
        super(unsigned, maxLen);
    }

    @Override
    public void setUnsigned(boolean unsigned) {
        super.setUnsigned(unsigned);
        if (unsigned) {
            super.setMin(0L);
            super.setMaxVal(65545);
        } else {
            super.setMin((long) Short.MIN_VALUE);
            super.setMax((long) Short.MAX_VALUE);
        }
    }
}
