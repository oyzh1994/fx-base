package cn.oyzh.fx.plus.controls.date;

/**
 * @author oyzh
 * @since 2023/12/24
 */
public class DateTimePicker extends CalendarPicker<DateTimeSelector> {

    public DateTimePicker() {
        super();
        this.setPattern("yyyy-MM-dd HH:mm:ss");
    }

    @Override
    public void showPopup() {
        if (this.selector == null) {
            this.selector = new DateTimeSelector(this);
        }
        super.showPopup();
    }
}
