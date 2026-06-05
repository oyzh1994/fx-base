package cn.oyzh.fx.gui.text.field;

import cn.oyzh.fx.gui.skin.TimeTextFieldSkin;
import javafx.scene.control.Skin;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

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
    }

    @Override
    public Timestamp getValue() {
        if (!this.isEmpty()) {
            try {
                SimpleDateFormat format = this.getDateFormat() == null ? FORMAT : this.getDateFormat();
                java.util.Date utilDate = format.parse(this.getText());
                return new Timestamp(utilDate.getTime());
            } catch (ParseException ex) {
                throw new RuntimeException(ex);
            }
        }
        return null;
    }

    @Override
    public void setValue(Object val) {
        if (val instanceof java.util.Date date) {
            SimpleDateFormat format = this.getDateFormat() == null ? FORMAT : this.getDateFormat();
            this.setText(format.format(date));
        } else {
            super.setValue(val);
        }
    }

    @Override
    protected Skin<?> createDefaultSkin() {
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
