package cn.oyzh.fx.gui.text.field;

import cn.oyzh.common.date.DateUtil;
import cn.oyzh.common.date.LocalDateTimeUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.gui.skin.DateTimeTextFieldSkin;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 日期时间文本输入框
 *
 * @author oyzh
 * @since 2024-07-19
 */
public class DateTimeTextField extends LimitTextField {

    /**
     * 默认日期时间格式
     */
    public static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 日期时间格式（不含秒）
     */
    public static final SimpleDateFormat FORMAT_1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    /**
     * ISO 日期时间格式
     */
    public static final SimpleDateFormat FORMAT_T = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * ISO 日期时间格式（不含秒）
     */
    public static final SimpleDateFormat FORMAT_T_1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");

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

    /**
     * 获取时间戳
     *
     * @return 时间戳，内容为空返回 null
     * @throws ParseException 文本解析失败时抛出
     */
    public Timestamp getTimestamp() throws ParseException {
        if (!this.isEmpty()) {
            String text = this.getText();
            java.util.Date utilDate;
            if (this.getDateFormat() != null) {
                utilDate = this.getDateFormat().parse(this.getText());
            } else if (text.contains("T")) {
                utilDate = FORMAT_T.parse(this.getText());
            } else {
                utilDate = FORMAT.parse(this.getText());
            }
            return new Timestamp(utilDate.getTime());
        }
        return null;
    }

    @Override
    public Object getValue() {
        String text = this.getText();
        if (!this.isEmpty() && !("CURRENT_TIMESTAMP".equalsIgnoreCase(text) || "CURRENT_TIMESTAMP()".equalsIgnoreCase(text))) {
            try {
                SimpleDateFormat format = null;
                if (this.getDateFormat() != null) {
                    format = this.getDateFormat();
                } else if (text.contains("T")) {
                    format = FORMAT_T;
                } else if (text.contains(":")) {
                    format = FORMAT;
                }
                if (format != null) {
                    return format.parse(text);
                }
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
        }
        if (super.value() instanceof Date date) {
            return date;
        }
        if (super.value() instanceof LocalDateTime time) {
            return DateUtil.of(time);
        }
        return text;
    }

    @Override
    public void formatValue() {
        SimpleDateFormat format;
        if (this.getDateFormat() == null) {
            format = getFormat(super.value());
        } else {
            format = this.getDateFormat();
        }
        if (super.value() instanceof LocalDateTime localDateTime) {
            this.setText(LocalDateTimeUtil.format(localDateTime, format.toPattern()));
        } else if (super.value() instanceof java.util.Date date) {
            this.setText(format.format(date));
        } else if (super.value() instanceof CharSequence s) {
            this.setText(s.toString());
        }
    }

    @Override
    public DateTimeTextFieldSkin skin() {
        return (DateTimeTextFieldSkin) super.skin();
    }

    @Override
    protected DateTimeTextFieldSkin createDefaultSkin() {
        return new DateTimeTextFieldSkin(this);
    }

    /**
     * 根据值的形态选择合适的日期时间格式
     *
     * @param value 值
     * @return 日期时间格式
     */
    private static SimpleDateFormat getFormat(Object value) {
        if (value == null) {
            return null;
        }
        long count = StringUtil.count(value.toString(), ':');
        SimpleDateFormat format;
        if (value.toString().contains("T")) {
            format = count == 2 || count == 0 ? FORMAT_T : FORMAT_T_1;
        } else {
            format = count == 2 || count == 0 ? FORMAT : FORMAT_1;
        }
        return format;
    }

    /**
     * 将值格式化为日期时间字符串
     *
     * @param value 值
     * @return 日期时间字符串
     */
    public static String format(Object value) {
        if (value == null) {
            return null;
        }
        SimpleDateFormat format = getFormat(value);
        if (value instanceof LocalDateTime localDateTime) {
            return LocalDateTimeUtil.format(localDateTime, format.toPattern());
        }
        if (value instanceof java.util.Date date) {
            return format.format(date);
        }
        return value.toString();
    }
}
