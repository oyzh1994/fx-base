package cn.oyzh.fx.db;

import java.util.List;

/**
 *
 * @author oyzh
 * @since 2026-11-25
 */
public interface DBTypeDefinition extends DBName {

    /**
     * 获取对象类型
     *
     * @return 结果
     */
    String getObjectType();

    /**
     * 设置对象类型
     *
     * @param objectType 对象类型
     */
    void setObjectType(String objectType);

    /**
     * 获取依赖
     *
     * @return 结果
     */
    default List<String> getDependencies() {
        return List.of();
    }

    /**
     * 设置依赖
     *
     * @param dependencies 依赖
     */
    default void setDependencies(List<String> dependencies) {

    }

    /**
     * 获取创建定义
     *
     * @return 结果
     */
    String getCreateDefinition();

    /**
     * 设置创建定义
     *
     * @param createDefinition 创建定义
     */
    void setCreateDefinition(String createDefinition);
}
