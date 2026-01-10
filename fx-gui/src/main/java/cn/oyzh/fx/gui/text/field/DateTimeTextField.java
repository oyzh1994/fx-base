package cn.oyzh.fx.gui.text.field;

import cn.oyzh.common.date.LocalDateTimeUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.gui.skin.DateTimeTextFieldSkin;
import javafx.scene.control.Skin;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author oyzh
 * @since 2024/07/19
 */
public class DateTimeTextField extends LimitTextField {

    public static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static final SimpleDateFormat FORMAT_1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public static final SimpleDateFormat FORMAT_2 = new SimpleDateFormat("yyyy-MM-dd HH");

    public static final SimpleDateFormat FORMAT_T = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public static final SimpleDateFormat FORMAT_T_1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");

    public static final SimpleDateFormat FORMAT_T_2 = new SimpleDateFormat("yyyy-MM-dd'T'HH");

    public Timestamp getValue() throws ParseException {
        if (!this.isEmpty()) {
            String text = this.getText();
            if (text.contains("T")) {
                java.util.Date utilDate = FORMAT_T.parse(this.getText());
                return new Timestamp(utilDate.getTime());
            }
            java.util.Date utilDate = FORMAT.parse(this.getText());
            return new Timestamp(utilDate.getTime());
        }
        return null;
    }

    public Date getDateValue() throws ParseException {
        if (!this.isEmpty()) {
            String text = this.getText();
            if (text.contains("T")) {
                return FORMAT_T.parse(this.getText());
            }
            return FORMAT.parse(this.getText());
        }
        return null;
    }

    public Object getObjectValue() {
        try {
            return this.getDateValue();
        } catch (ParseException ignored) {
        }
        return this.getText();
    }

    @Override
    public void setValue(Object val) {
        SimpleDateFormat format = getFormat(val);
        if (val instanceof LocalDateTime localDateTime) {
            this.setText(LocalDateTimeUtil.format(localDateTime, format.toPattern()));
        } else if (val instanceof java.util.Date date) {
            this.setText(format.format(date));
        } else {
            super.setValue(val);
        }
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new DateTimeTextFieldSkin(this);
    }

    private static SimpleDateFormat getFormat(Object value) {
        if (value == null) {
            return null;
        }
        long count = StringUtil.count(value.toString(), ':');
        SimpleDateFormat format;
        if (value.toString().contains("T")) {
            format = count == 3 ? FORMAT_T : count == 2 ? FORMAT_T_1 : FORMAT_T_2;
        } else {
            format = count == 3 ? FORMAT : count == 2 ? FORMAT_1 : FORMAT_2;
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
