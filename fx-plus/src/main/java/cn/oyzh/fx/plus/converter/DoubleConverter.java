package cn.oyzh.fx.plus.converter;

import cn.hutool.core.util.NumberUtil;
import javafx.util.StringConverter;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

/**
 * @author oyzh
 * @since 2023/9/28
 */
public class DoubleConverter extends StringConverter<Double> {

    @Override
    public String toString(Double var1) {
        return var1 == null?"":var1.toString();
    }

    @Override
    public Double fromString(String s) {
        if (NumberUtil.isDouble(s)) {
            return NumberUtil.parseDouble(s);
        }
        return null;
    }
}
