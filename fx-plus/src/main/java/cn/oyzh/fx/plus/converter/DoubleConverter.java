package cn.oyzh.fx.plus.converter;

import cn.oyzh.common.util.NumberUtil;
import cn.oyzh.common.util.RegexUtil;
import javafx.util.StringConverter;

/**
 * @author oyzh
 * @since 2023/9/28
 */
public class DoubleConverter extends StringConverter<Double> {

    @Override
    public String toString(Double var1) {
        return var1 == null ? "" : var1.toString();
    }

    @Override
    public Double fromString(String s) {
        if (RegexUtil.isDecimal(s) || RegexUtil.isNumber(s)) {
            return NumberUtil.parseDouble(s);
        }
        return null;
    }
}
