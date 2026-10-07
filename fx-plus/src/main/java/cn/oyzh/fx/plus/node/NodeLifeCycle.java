package cn.oyzh.fx.plus.node;

import cn.oyzh.fx.plus.adapter.PropAdapter;

/**
 * 节点生命周期，用于标记节点是否已初始化以及节点销毁时的回调处理
 *
 * @author oyzh
 * @since 2024-11-18
 */
public interface NodeLifeCycle extends PropAdapter {

    /**
     * 节点销毁时回调，清除初始化标记
     */
    default void onNodeDestroy(){
        this.removeProp("node:initialize");
    }

    /**
     * 节点初始化时回调，设置初始化标记
     */
    default void onNodeInitialize() {
        this.setProp("node:initialize", true);
    }

    /**
     * 节点是否已初始化
     *
     * @return 结果
     */
    default boolean isNodeInitialize() {
        return this.hasProp("node:initialize");
    }
}
