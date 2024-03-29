package cn.oyzh.fx.plus.test;

import cn.oyzh.fx.plus.event.Event;
import cn.oyzh.fx.plus.event.EventFormatter;
import cn.oyzh.fx.plus.event.EventListener;
import com.google.common.eventbus.Subscribe;

/**
 * @author oyzh
 * @since 2024/3/29
 */
public class TestEventListener implements EventListener {

    @Subscribe
    public void test(TestEvent event) {
        System.out.println(event.data());
    }

    @Subscribe
    private void test1(TestEvent event) {
        System.out.println(event.data());
    }

    @Subscribe
    private void test2(Event<?> event) {
        System.out.println(event.data());
    }

    @Subscribe
    private void test3(EventFormatter formatter) {
        System.out.println(formatter.eventFormat());
    }
}
