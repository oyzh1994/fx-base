package cn.oyzh.fx.gui.text.field;


import cn.oyzh.common.util.NumberUtil;

import java.util.Date;

/**
 * 年份文本输入框
 *
 * @author oyzh
 * @since 2024-07-19
 */
public class YearTextField extends NumberTextField {

    /**
     * 构造年份文本输入框
     */
    public YearTextField() {
        super(null);
    }

    @Override
    public void setValue(Object value) {
        if (value instanceof CharSequence sequence) {
            String str = sequence.toString();
            if (str.contains("-")) {
                this.value(NumberUtil.parseNumber(str.split("-")[0]));
            } else {
                this.value(NumberUtil.parseNumber(str));
            }
        } else if (value instanceof Date date) {
            this.value(1900 + date.getYear());
        }
    }

    /**
     * 将值格式化为年份字符串
     *
     * @param value 值
     * @return 年份字符串
     */
    public static String format(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof CharSequence sequence) {
            String str = sequence.toString();
            if (str.contains("-")) {
                return NumberUtil.parseNumber(str.split("-")[0]).intValue() + "";
            } else {
                return NumberUtil.parseNumber(str).intValue() + "";
            }
        }
        if (value instanceof Date date) {
            return 1900 + date.getYear() + "";
        }
        return value.toString();
    }
}
