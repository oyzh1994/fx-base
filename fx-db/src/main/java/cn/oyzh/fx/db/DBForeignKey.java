package cn.oyzh.fx.db;


import cn.oyzh.common.util.StringUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 外键接口，用于校验外键名称是否有效
 *
 * @author oyzh
 * @since 2026-09-02
 */
public interface DBForeignKey extends DBName {

    /**
     * 是否无效
     *
     * @return 结果
     */
    default boolean isInvalid() {
        return StringUtil.isBlank(this.getName());
    }

    /**
     * 获取外键字段列表
     *
     * @return 外键字段列表
     */
    Set<String> getColumns();

    /**
     * 设置外键字段列表
     *
     * @param columns 外键字段列表
     */
    void setColumns(Set<String> columns);

    /**
     * 添加外键字段列表
     *
     * @param column 外键字段
     */
    default void addColumn(String column) {
        if (this.getColumns() == null) {
            this.setColumns(new HashSet<>());
        }
        this.getColumns().add(column);
    }

    /**
     * 获取引用库名称
     *
     * @return 引用库名称
     */
    String getReferencedDatabase();

    /**
     * 设置引用库名称
     *
     * @param referencedDatabase 引用库名称
     */
    void setReferencedDatabase(String referencedDatabase);

    /**
     * 获取引用表名称
     *
     * @return 引用表名称
     */
    String getReferencedTable();

    /**
     * 设置引用表名称
     *
     * @param referencedTable 引用表名称
     */
    void setReferencedTable(String referencedTable);

    /**
     * 获取引用字段列表
     *
     * @return 引用字段列表
     */
    Set<String> getReferencedColumns();

    /**
     * 设置引用字段列表
     *
     * @param referencedColumns 引用字段列表
     */
    void setReferencedColumns(Set<String> referencedColumns);

    /**
     * 添加引用字段
     *
     * @param columnName 字段名称
     */
    default void addReferencedColumn(String columnName) {
        if (this.getReferencedColumns() == null) {
            this.setReferencedColumns(new HashSet<>());
        }
        this.getReferencedColumns().add(columnName);
    }
}
