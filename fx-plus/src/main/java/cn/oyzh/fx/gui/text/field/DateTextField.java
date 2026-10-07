package cn.oyzh.fx.gui.text.field;

import cn.oyzh.fx.gui.skin.DateTextFieldSkin;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 日期文本输入框
 *
 * @author oyzh
 * @since 2024/07/19
 */
public class DateTextField extends LimitTextField {

    /**
     * 默认日期格式
     */
    public static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * 自定义日期格式
     */
    private SimpleDateFormat dateFormat;

    /**
     * 获取自定义日期格式
     *
     * @return 自定义日期格式
     */
    public SimpleDateFormat getDateFormat() {
        return dateFormat;
    }

    /**
     * 设置自定义日期格式
     *
     * @param dateFormat 自定义日期格式
     */
    public void setDateFormat(SimpleDateFormat dateFormat) {
        this.dateFormat = dateFormat;
        if (dateFormat != null) {
            this.skin().setFormatter(DateTimeFormatter.ofPattern(dateFormat.toPattern()));
        } else {
            this.skin().setFormatter(null);
        }
    }

    @Override
    public Object getValue() {
        String text = this.getText();
        if (!this.isEmpty() && !"CURRENT_TIMESTAMP".equalsIgnoreCase(text)) {
            try {
                SimpleDateFormat format = this.getDateFormat() == null ? FORMAT : this.getDateFormat();
                return format.parse(text);
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
        }
        if (super.value() instanceof Date date) {
            return date;
        }
        if (super.value() instanceof java.util.Date date) {
            return new Date(date.getTime());
        }
        return text;
    }

    @Override
    public void formatValue() {
        if (super.value() instanceof java.util.Date date) {
            SimpleDateFormat format = this.getDateFormat() == null ? FORMAT : this.getDateFormat();
            this.setText(format.format(date));
        }
    }

    @Override
    public DateTextFieldSkin skin() {
        return (DateTextFieldSkin) super.skin();
    }

    @Override
    protected DateTextFieldSkin createDefaultSkin() {
        return new DateTextFieldSkin(this);
    }

    /**
     * 将值格式化为日期字符串
     *
     * @param value 值
     * @return 日期字符串
     */
    public static String format(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof java.util.Date date) {
            return FORMAT.format(date);
        }
        return value.toString();
    }
}
