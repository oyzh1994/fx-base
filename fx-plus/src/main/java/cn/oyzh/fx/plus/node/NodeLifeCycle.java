package cn.oyzh.fx.plus.node;

/**
 * @author oyzh
 * @since 2024-11-18
 */
public interface NodeLifeCycle {

    void onNodeDestroy();

    void onNodeInitialize();
}
