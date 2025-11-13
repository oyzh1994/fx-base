package cn.oyzh.fx.pkg;

/**
 * @author oyzh
 * @since 2025-11-12
 */
public interface ConfigMargeAble<T> {

    /**
     * 合并配置
     *
     * @param config 配置
     */
    void marge(T config);
}
