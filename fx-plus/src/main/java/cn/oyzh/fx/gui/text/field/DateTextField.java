package cn.oyzh.fx.gui.text.field;

import cn.oyzh.fx.gui.skin.DateTextFieldSkin;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;

/**
 * @author oyzh
 * @since 2024/07/19
 */
public class DateTextField extends LimitTextField {

    public static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd");

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

    @Override
    public Date getValue() {
        if (!this.isEmpty()) {
            try {
                SimpleDateFormat format = this.getDateFormat() == null ? FORMAT : this.getDateFormat();
                java.util.Date utilDate = format.parse(this.getText());
                return new Date(utilDate.getTime());
            } catch (ParseException ex) {
                throw new RuntimeException(ex);
            }
        }
        if (super.getValue() instanceof Date date) {
            return date;
        }
        if (super.getValue() instanceof java.util.Date date) {
            return new Date(date.getTime());
        }
        return null;
    }

    @Override
    public void formatValue() {
        if (super.getValue() instanceof java.util.Date date) {
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
