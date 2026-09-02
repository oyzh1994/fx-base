package cn.oyzh.fx.db;


/**
 * @author oyzh
 * @since 2024/1/30
 */
public interface DBSchema {

    /**
     * 获取名称
     *
     * @return 结果
     */
    String getName();

    /**
     * 设置名称
     *
     * @param name 名称
     */
    void setName(String name);
}
