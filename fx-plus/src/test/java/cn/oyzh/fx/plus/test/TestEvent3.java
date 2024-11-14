package cn.oyzh.fx.plus.test;

import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;

/**
 * @author oyzh
 * @since 2024/3/29
 */
public class TestEvent3 extends Event<String> implements EventFormatter {

    @Override
    public String eventFormat() {
        return "Format:" + this.data();
    }
}
