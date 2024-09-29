package cn.oyzh.fx.plus.converter;

import cn.oyzh.fx.common.util.NumberUtil;
import cn.oyzh.fx.common.util.RegexUtil;
import javafx.util.StringConverter;

import java.math.BigDecimal;

/**
 * @author oyzh
 * @since 2023/12/28
 */
public class BigDecimalConverter extends StringConverter<BigDecimal> {

    @Override
    public String toString(BigDecimal var1) {
        return var1 == null ? "" : var1.toString();
    }

    @Override
    public BigDecimal fromString(String s) {
        if (RegexUtil.isNumber(s)) {
            return NumberUtil.toBigDecimal(s);
        }
        return null;
    }
}
