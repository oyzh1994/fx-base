package cn.oyzh.fx.plus.node;

import cn.oyzh.fx.plus.adapter.PropAdapter;

/**
 * @author oyzh
 * @since 2024-11-18
 */
public interface NodeLifeCycle extends PropAdapter {

    default void onNodeDestroy(){
        this.removeProp("node:initialize");
    }

    default void onNodeInitialize() {
        this.setProp("node:initialize", true);
    }

    default boolean isNodeInitialize() {
        return this.hasProp("node:initialize");
    }
}
