package cn.oyzh.fx.plus.controls.textfield;

import cn.oyzh.fx.plus.controls.FlexHBox;

/**
 * bit文本域
 *
 * @author oyzh
 * @since 2023/12/22
 */
public class TimeTextField extends FlexHBox {

    {
        NumberTextField hour = new NumberTextField();
        NumberTextField minute = new NumberTextField();
        NumberTextField second = new NumberTextField();

        hour.setFlexWidth("33%");
        hour.setFlexHeight("100%");

        minute.setFlexWidth("33%");
        minute.setFlexHeight("100%");

        second.setFlexWidth("33%");
        second.setFlexHeight("100%");

        this.addChild(hour);
        this.addChild(minute);
        this.addChild(second);
    }
}
