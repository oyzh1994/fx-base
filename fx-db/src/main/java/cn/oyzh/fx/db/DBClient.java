package cn.oyzh.fx.db;

import java.util.List;
import java.util.Map;

/**
 * 数据库客户端接口
 *
 * @author oyzh
 * @since 2026-09-04
 */
public interface DBClient {

    /**
     * 获取数据库连接管理器
     *
     * @return 结果
     */
    DBConnManager getConnManager();

    /**
     * 获取属性列表
     *
     * @return 结果
     */
    Map<String, Object> getProperties();

    /**
     * 获取属性
     *
     * @param key 键
     * @param <T> 属性类型
     * @return 属性
     */
    default <T> T getProperty(String key) {
        return this.getProperties() == null || key == null ? null : (T) this.getProperties().get(key);
    }

    /**
     * 是否有此属性
     *
     * @param key 键
     * @return 结果
     */
    default boolean hasProperty(String key) {
        return this.getProperties() != null && this.getProperties().containsKey(key);
    }

    /**
     * 添加属性
     *
     * @param key   键
     * @param value 值
     */
    default void putProperty(String key, Object value) {
        if (key != null && value != null) {
            this.getProperties().put(key, value);
        }
    }

    // ===== JDBC 通用方法 =====

    /**
     * 是否只读模式
     *
     * @return 结果
     */
    boolean isReadonly();

    /**
     * 获取数据库方言
     *
     * @return 方言
     */
    DBDialect dialect();

    /**
     * 获取表数量
     *
     * @param dbName 库名称或者模式名称
     * @return 表数量
     */
    int tableSize(String dbName);

    /**
     * 获取视图数量
     *
     * @param dbName 库名称或者模式名称
     * @return 视图数量
     */
    int viewSize(String dbName);

    /**
     * 获取存储过程数量
     *
     * @param dbName 库名称或者模式名称
     * @return 存储过程数量
     */
    int procedureSize(String dbName);

    /**
     * 获取函数数量
     *
     * @param dbName 库名称或者模式名称
     * @return 函数数量
     */
    int functionSize(String dbName);

    /**
     * 获取数据库版本
     *
     * @return 版本
     */
    String selectVersion();

    /**
     * 获取数据库产品信息
     *
     * @return 产品信息
     */
    String selectProduct();

    /**
     * 批量插入SQL
     *
     * @param dbName 库名称
     * @param sqlList SQL列表
     * @return 插入行数
     */
    int insertBatch(String dbName, List<String> sqlList);

    /**
     * 是否支持指定特性
     *
     * @param feature 特性
     * @return 结果
     */
    boolean isSupportFeature(DBFeature feature);

    /**
     * 是否支持检查约束特性
     *
     * @return 结果
     */
    default boolean isSupportCheckFeature() {
        return this.isSupportFeature(DBFeature.CHECK);
    }

    /**
     * 是否支持事件/调度器特性
     *
     * @return 结果
     */
    default boolean isSupportEventFeature() {
        return this.isSupportFeature(DBFeature.EVENT);
    }
}
