package cn.oyzh.fx.db;

/**
 * 数据库索引通用接口
 * <p>
 * 各数据库的索引模型实现此接口后，即可复用同一套索引树节点与索引设计页面。
 *
 * @author oyzh
 * @since 2026-11-28
 */
public interface DBIndex extends DBName{

    /**
     * 获取所属表（集合）名称
     *
     * @return 表名称
     */
    String getTableName();

    /**
     * 设置所属表（集合）名称
     *
     * @param tableName 表名称
     */
    void setTableName(String tableName);
}
