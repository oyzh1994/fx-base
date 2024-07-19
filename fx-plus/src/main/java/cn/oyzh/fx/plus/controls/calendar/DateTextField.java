package cn.oyzh.fx.plus.controls.calendar;

import cn.oyzh.fx.plus.controls.textfield.LimitTextField;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * @author oyzh
 * @since 2024/07/19
 */
public class DateTextField extends LimitTextField {

    {
        this.setSkin(new DateTextFieldSkin(this));
    }

    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public Date getValue() throws ParseException {
        if (!this.isEmpty()) {
            java.util.Date utilDate = FORMAT.parse(this.getText());
            return new Date(utilDate.getTime());
        }
        return null;
    }
}
