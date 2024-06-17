package cn.oyzh.fx.pkg;

import cn.oyzh.fx.pkg.config.ExtPackrConfig;

/**
 * 处理器
 *
 * @author oyzh
 * @since 2024/6/14
 */
public interface Handler {

    /**
     * 唯一的
     *
     * @return 结果
     */
    default boolean unique() {
        return false;
    }

    /**
     * 获取排序
     *
     * @return 排序
     */
    default int order() {
        return PackOrder.MID;
    }

    /**
     * 获取名称
     *
     * @return 名称
     */
    String name();

    /**
     * 处理
     *
     * @param packrConfig 打包配置
     * @throws Exception 异常
     */
    void handle(ExtPackrConfig packrConfig) throws Exception;

}
