package cn.oyzh.fx.plus.converter;

import cn.hutool.core.util.NumberUtil;
import javafx.util.StringConverter;

/**
 * @author oyzh
 * @since 2023/9/28
 */
public class LongConverter extends StringConverter<Long> {

    @Override
    public String toString(Long var1) {
        return var1 == null ? "" : var1.toString();
    }

    @Override
    public Long fromString(String s) {
        if (NumberUtil.isLong(s) || NumberUtil.isNumber(s)) {
            return NumberUtil.parseLong(s);
        }
        return null;
    }
}
