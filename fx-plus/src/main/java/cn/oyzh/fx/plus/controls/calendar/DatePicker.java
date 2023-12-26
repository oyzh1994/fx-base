package cn.oyzh.fx.plus.controls.calendar;

/**
 * @author oyzh
 * @since 2023/12/26
 */
public class DatePicker extends CalendarPicker<DateSelector> {

    public DatePicker() {
        super();
        this.setPattern("yyyy-MM-dd");
    }

    @Override
    public void showPopup() {
        if (this.selector == null) {
            this.selector = new DateSelector(this);
        }
        super.showPopup();
    }
}
