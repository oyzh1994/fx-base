package cn.oyzh.fx.plus.event;

import javafx.event.Event;
import javafx.event.EventType;

/**
 * 匿名事件，用于携带任意类型数据源的通用事件
 *
 * @author oyzh
 * @since 2024-10-10
 */
public class AnonymousEvent<E> extends Event {

    /**
     * 匿名事件类型
     */
    public static final EventType<AnonymousEvent<?>> ANONYMOUS_EVENT = new EventType<>("SEARCH_TRIGGER_EVENT");

    /**
     * 构造匿名事件对象。
     *
     * @param source 源
     */
    public AnonymousEvent(E source) {
        super(source, null, ANONYMOUS_EVENT);
    }

    /**
     * 创建匿名事件
     *
     * @param source 事件源
     * @param <E>    事件源类型
     * @return 匿名事件
     */
    public static <E> AnonymousEvent<E> of(E source) {
        return new AnonymousEvent<E>(source);
    }
}
