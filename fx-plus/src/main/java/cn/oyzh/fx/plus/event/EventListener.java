package cn.oyzh.fx.plus.event;

/**
 * 事件监听接口
 *
 * @author oyzh
 * @since 2024/3/29
 */
public interface EventListener {

    /**
     * 注册监听器
     */
    default void register() {
        EventUtil.register(this);
    }

    /**
     * 取消注册监听器
     */
    default void unregister() {
        EventUtil.unregister(this);
    }
}
