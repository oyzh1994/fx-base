package cn.oyzh.fx.plus.controls.calendar;

import cn.oyzh.fx.plus.controls.textfield.LimitTextField;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * @author oyzh
 * @since 2024/07/21
 */
public class TimeTextField extends LimitTextField {

    {
        this.setSkin(new TimeTextFieldSkin(this));
    }

    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("HH:mm:ss");

    public Timestamp getValue() throws ParseException {
        if (!this.isEmpty()) {
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
}
