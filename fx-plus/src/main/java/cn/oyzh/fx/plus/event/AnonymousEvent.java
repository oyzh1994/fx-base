package cn.oyzh.fx.plus.event;

import javafx.event.Event;
import javafx.event.EventType;

/**
 * @author oyzh
 * @since 2024-10-10
 */
public class AnonymousEvent<E> extends Event {

    public static final EventType<AnonymousEvent<?>> ANONYMOUS_EVENT = new EventType<>("SEARCH_TRIGGER_EVENT");

    public AnonymousEvent(E source) {
        super(source, null, ANONYMOUS_EVENT);
    }

    public static <E> AnonymousEvent<E> of(E source) {
        return new AnonymousEvent<E>(source);
    }
}
