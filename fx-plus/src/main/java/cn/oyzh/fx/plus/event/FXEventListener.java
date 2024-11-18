package cn.oyzh.fx.plus.event;

import cn.oyzh.event.EventListener;
import cn.oyzh.fx.plus.node.NodeLifeCycle;

/**
 * @author oyzh
 * @since 2024-11-18
 */
public interface FXEventListener extends EventListener, NodeLifeCycle {

    @Override
    default void onNodeDestroy() {
        EventListener.super.unregister();
    }

    @Override
    default void onNodeInitialize() {
        EventListener.super.register();
    }
}
