package cn.oyzh.fx.plus.controls.digital;

/**
 *  mid int文本域
 *
 * @author oyzh
 * @since 2023/12/27
 */
public class MidIntTextField extends IntTextField {

    public MidIntTextField() {
        super(false);
    }

    public MidIntTextField(boolean unsigned) {
        super(unsigned);
    }

    @Override
    public void setUnsigned(boolean unsigned) {
        super.setUnsigned(unsigned);
        if (unsigned) {
            super.setMin(0L);
            super.setMaxVal(16_777_215L);
        } else {
            super.setMin(-8_388_608L);
            super.setMax(8_388_608L);
        }
    }
}
