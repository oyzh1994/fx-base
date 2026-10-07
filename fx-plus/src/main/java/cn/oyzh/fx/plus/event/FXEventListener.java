package cn.oyzh.fx.plus.event;

import cn.oyzh.event.EventListener;
import cn.oyzh.fx.plus.node.NodeLifeCycle;

/**
 * fx 事件监听器，绑定节点生命周期，在节点初始化时注册、节点销毁时注销
 *
 * @author oyzh
 * @since 2024-11-18
 */
public interface FXEventListener extends EventListener, NodeLifeCycle {

    @Override
    default void onNodeDestroy() {
        NodeLifeCycle.super.onNodeDestroy();
        this.unregister();
    }

    @Override
    default void onNodeInitialize() {
        if (!NodeLifeCycle.super.isNodeInitialize()) {
            NodeLifeCycle.super.onNodeInitialize();
            this.register();
        }
    }
}
