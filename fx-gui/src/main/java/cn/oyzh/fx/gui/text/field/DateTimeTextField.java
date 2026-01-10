package cn.oyzh.fx.gui.text.field;

import cn.oyzh.common.date.LocalDateTimeUtil;
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

    public static final SimpleDateFormat FORMAT_T = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

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
        if (val instanceof java.util.Date date) {
            this.setText(FORMAT.format(date));
        } else {
            super.setValue(val);
        }
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new DateTimeTextFieldSkin(this);
    }

    public static String format(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime localDateTime) {
            if (value.toString().contains("T")) {
                return LocalDateTimeUtil.format(localDateTime, FORMAT_T.toPattern());
            }
            return LocalDateTimeUtil.format(localDateTime, FORMAT.toPattern());
        }
        if (value instanceof java.util.Date date) {
            if (value.toString().contains("T")) {
                return FORMAT_T.format(value);
            }
            return FORMAT.format(date);
        }
        return value.toString();
    }
}
