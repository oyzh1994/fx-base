package cn.oyzh.fx.plus.converter;

import cn.hutool.core.util.NumberUtil;
import javafx.util.StringConverter;

/**
 * @author oyzh
 * @since 2023/12/28
 */
public class NumberConverter extends StringConverter<Number> {

    @Override
    public String toString(Number var1) {
        return var1 == null ? "" : var1.toString();
    }

    @Override
    public Number fromString(String s) {
        if (NumberUtil.isDouble(s) || NumberUtil.isLong(s) || NumberUtil.isNumber(s)) {
            return NumberUtil.parseNumber(s);
        }
        return null;
    }
}
