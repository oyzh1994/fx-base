package cn.oyzh.fx.plus.test;

import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.event.EventListener;
import cn.oyzh.event.EventSubscribe;

/**
 * 事件监听器测试，验证多种事件订阅方法的触发
 *
 * @author oyzh
 * @since 2024-03-29
 */
public class TestEventListener implements EventListener {

    @EventSubscribe
    public void test(TestEvent event) {
        System.out.println(event.data());
    }

    @EventSubscribe
    private void test1(TestEvent event) {
        System.out.println(event.data());
    }

    @EventSubscribe
    private void test2(Event<?> event) {
        System.out.println(event.data());
    }

    @EventSubscribe
    private void test3(EventFormatter formatter) {
        System.out.println(formatter.eventFormat());
    }
}
