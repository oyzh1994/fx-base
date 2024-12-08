package cn.oyzh.fx.gui.textfield;


import cn.oyzh.common.util.NumberUtil;

import java.util.Date;

/**
 * 年份文本输入框
 *
 * @author oyzh
 * @since 2024/07/19
 */
public class YearTextField extends NumberTextField {

    public YearTextField() {
        super(true, null);
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

    public static String format(Object value) {
        if (value instanceof CharSequence sequence) {
            String str = sequence.toString();
            if (str.contains("-")) {
                return NumberUtil.parseNumber(str.split("-")[0]).intValue() + "";
            } else {
                return NumberUtil.parseNumber(str).intValue() + "";
            }
        } else if (value instanceof Date date) {
            return 1900 + date.getYear() + "";
        }
        return null;
    }
}
