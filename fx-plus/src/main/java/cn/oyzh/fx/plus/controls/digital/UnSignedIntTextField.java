package cn.oyzh.fx.plus.controls.digital;

/**
 * integer文本域
 *
 * @author oyzh
 * @since 2023/12/22
 */
public class UnSignedIntTextField extends IntTextField {

    {
        super.setMin(0L);
        super.setMax(4294967295L);
    }
}
