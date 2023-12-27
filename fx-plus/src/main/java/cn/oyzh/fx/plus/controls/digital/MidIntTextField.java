package cn.oyzh.fx.plus.controls.digital;

/**
 *  mid int文本域
 *
 * @author oyzh
 * @since 2023/12/27
 */
public class MidIntTextField extends NumberTextField {

    {
        super.setMin((long) -8_388_608);
        super.setMax((long) 8_388_607);
        // super.setMax((long) 16_777_215);
    }
}
