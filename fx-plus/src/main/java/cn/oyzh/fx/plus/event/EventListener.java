package cn.oyzh.fx.plus.event;

/**
 * @author oyzh
 * @since 2024/3/29
 */
public interface EventListener {

    default void register() {
        EventUtil.register(this);
    }

    default void unregister() {
        EventUtil.unregister(this);
    }
}
