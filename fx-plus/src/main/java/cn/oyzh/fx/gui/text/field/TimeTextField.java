package cn.oyzh.fx.gui.text.field;

import cn.oyzh.fx.gui.skin.TimeTextFieldSkin;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @author oyzh
 * @since 2024/07/21
 */
public class TimeTextField extends LimitTextField {

    public static final SimpleDateFormat FORMAT = new SimpleDateFormat("HH:mm:ss");

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
        if (super.getValue() instanceof Date utilDate) {
            return new Timestamp(utilDate.getTime());
        }
        if (super.getValue() instanceof Timestamp timestamp) {
            return timestamp;
        }
        return text;
    }

    @Override
    public void formatValue() {
        if (super.getValue() instanceof java.util.Date date) {
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
