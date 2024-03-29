package cn.oyzh.fx.plus.test;

import cn.oyzh.fx.plus.event.Event;
import cn.oyzh.fx.plus.event.EventFormatter;

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
