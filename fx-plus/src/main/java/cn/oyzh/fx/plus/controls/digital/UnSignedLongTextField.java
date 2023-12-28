package cn.oyzh.fx.plus.controls.digital;

import java.math.BigDecimal;

/**
 * long文本域
 *
 * @author oyzh
 * @since 2023/12/22
 */
public class UnSignedLongTextField extends LongTextField {

    {
        super.setMin(0L);
        super.setMaxVal(new BigDecimal("18446744073709551615"));
    }
}
