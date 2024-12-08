package cn.oyzh.fx.gui.text.field;

import cn.oyzh.fx.gui.skin.DateTextFieldSkin;
import javafx.scene.control.Skin;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * @author oyzh
 * @since 2024/07/19
 */
public class DateTextField extends LimitTextField {

    public static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public Date getValue() throws ParseException {
        if (!this.isEmpty()) {
            java.util.Date utilDate = FORMAT.parse(this.getText());
            return new Date(utilDate.getTime());
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
        return new DateTextFieldSkin(this);
    }

    public static String format(Object value) {
        if (value instanceof java.util.Date date) {
            return FORMAT.format(date);
        }
        return null;
    }
}
