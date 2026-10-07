package cn.oyzh.fx.gui.text.field;

import cn.oyzh.fx.gui.skin.TimeTextFieldSkin;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 时间文本输入框
 *
 * @author oyzh
 * @since 2024-07-19
 */
public class TimeTextField extends LimitTextField {

    /**
     * 默认时间格式
     */
    public static final SimpleDateFormat FORMAT = new SimpleDateFormat("HH:mm:ss");

    /**
     * 自定义时间格式
     */
    private SimpleDateFormat dateFormat;

    /**
     * 获取自定义时间格式
     *
     * @return 自定义时间格式
     */
    public SimpleDateFormat getDateFormat() {
        return dateFormat;
    }

    /**
     * 设置自定义时间格式
     *
     * @param dateFormat 自定义时间格式
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
                Date utilDate = format.parse(text);
                return new Timestamp(utilDate.getTime());
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
        }
        if (super.value() instanceof Date utilDate) {
            return new Timestamp(utilDate.getTime());
        }
        if (super.value() instanceof Timestamp timestamp) {
            return timestamp;
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
    public TimeTextFieldSkin skin() {
        return (TimeTextFieldSkin) super.skin();
    }

    @Override
    protected TimeTextFieldSkin createDefaultSkin() {
        return new TimeTextFieldSkin(this);
    }

    /**
     * 将值格式化为时间字符串
     *
     * @param value 值
     * @return 时间字符串
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
