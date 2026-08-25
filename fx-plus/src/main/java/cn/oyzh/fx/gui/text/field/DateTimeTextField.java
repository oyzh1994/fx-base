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
 * @author oyzh
 * @since 2024/07/19
 */
public class DateTimeTextField extends LimitTextField {

    public static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static final SimpleDateFormat FORMAT_1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public static final SimpleDateFormat FORMAT_T = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public static final SimpleDateFormat FORMAT_T_1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");

    private SimpleDateFormat dateFormat;

    public SimpleDateFormat getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(SimpleDateFormat dateFormat) {
        this.dateFormat = dateFormat;
        if (dateFormat != null) {
            this.skin().setFormatter(DateTimeFormatter.ofPattern(dateFormat.toPattern()));
        } else {
            this.skin().setFormatter(null);
        }
    }

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
        if (!this.isEmpty() && !"CURRENT_TIMESTAMP".equalsIgnoreCase(text)) {
            try {
                SimpleDateFormat format;
                if (this.getDateFormat() != null) {
                    format = this.getDateFormat();
                } else if (text.contains("T")) {
                    format = FORMAT_T;
                } else {
                    format = FORMAT;
                }
                return format.parse(text);
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
        }
        if (super.getValue() instanceof Date date) {
            return date;
        }
        if (super.getValue() instanceof LocalDateTime time) {
            return DateUtil.of(time);
        }
        return text;
    }

    @Override
    public void formatValue() {
        SimpleDateFormat format;
        if (this.getDateFormat() == null) {
            format = getFormat(super.getValue());
        } else {
            format = this.getDateFormat();
        }
        if (super.getValue() instanceof LocalDateTime localDateTime) {
            this.setText(LocalDateTimeUtil.format(localDateTime, format.toPattern()));
        } else if (super.getValue() instanceof java.util.Date date) {
            this.setText(format.format(date));
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
