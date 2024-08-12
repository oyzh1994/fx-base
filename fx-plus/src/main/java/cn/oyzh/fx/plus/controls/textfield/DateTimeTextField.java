package cn.oyzh.fx.plus.controls.textfield;

import cn.oyzh.fx.plus.skin.DateTimeTextFieldSkin;
import javafx.scene.control.Skin;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * @author oyzh
 * @since 2024/07/19
 */
public class DateTimeTextField extends LimitTextField {

    public static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final SimpleDateFormat FORMAT_T = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

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
        if (value instanceof java.util.Date date) {
            return FORMAT.format(date);
        }
        return null;
    }
}
