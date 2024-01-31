package cn.oyzh.fx.plus.controls.digital;

/**
 * integer文本域
 *
 * @author oyzh
 * @since 2023/12/22
 */
public class IntTextField extends NumberTextField {

    public IntTextField() {
        super(false);
    }

    public IntTextField(boolean unsigned) {
        super(unsigned);
    }

    public IntTextField(boolean unsigned, Integer maxLen) {
        super(unsigned, maxLen);
    }

    @Override
    public void setUnsigned(boolean unsigned) {
        super.setUnsigned(unsigned);
        if (unsigned) {
            super.setMin(0L);
            super.setMaxVal(4_294_967_295L);
        } else {
            super.setMin((long) Integer.MIN_VALUE);
            super.setMax((long) Integer.MAX_VALUE);
        }
    }
}
