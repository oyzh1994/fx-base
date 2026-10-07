package cn.oyzh.fx.db;

import cn.oyzh.common.util.StringUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 数据库记录数据，保存列与值的映射
 *
 * @author oyzh
 * @since 2026-09-07
 */
public class DBRecordData extends HashMap<DBColumn, Object> {

    /**
     * 获取列名称集合
     *
     * @return 列名称集合
     */
    public Set<String> columns() {
        return super.keySet().parallelStream().map(DBColumn::getName).collect(Collectors.toSet());
    }

    /**
     * 获取有值的列名称集合
     *
     * @return 列名称集合
     */
    public Set<String> notNullColumns() {
        Set<String> columns = this.columns();
        return columns.parallelStream().filter(this::hasValue).collect(Collectors.toSet());
    }

    /**
     * 根据列名称查找列
     *
     * @param column 列名称
     * @return 列
     */
    public DBColumn column(String column) {
        for (DBColumn dbColumn : this.keySet()) {
            if (StringUtil.equalsAnyIgnoreCase(column, dbColumn.getName())) {
                return dbColumn;
            }
        }
        return null;
    }

    /**
     * 判断列是否有值
     *
     * @param column 列名称
     * @return 结果
     */
    public boolean hasValue(String column) {
        return this.value(column) != null;
    }

    /**
     * 获取列的值
     *
     * @param column 列名称
     * @return 值
     */
    public Object value(String column) {
        for (Map.Entry<DBColumn, Object> entry : this.entrySet()) {
            if (StringUtil.equalsAnyIgnoreCase(column, entry.getKey().getName())) {
                return entry.getValue();
            }
        }
        return null;
    }


    /**
     * 移除指定列
     *
     * @param column 列名称
     */
    public void remove(String column) {
        DBColumn dbColumn = this.column(column);
        if (dbColumn != null) {
            super.remove(dbColumn);
        }
    }

    /**
     * 获取所有键值对
     *
     * @return 键值对集合
     */
    public Set<Map.Entry<DBColumn, Object>> entries() {
        return super.entrySet();
    }

    /**
     * 获取列数量
     *
     * @return 列数量
     */
    public int columnSize() {
        return super.size();
    }

    /**
     * 判断列是否有值
     *
     * @param column 列名称
     * @return 结果
     */
    public boolean notNull(String column) {
        return this.value(column) != null;
    }

    /**
     * 判断列是否为几何类型
     *
     * @param column 列名称
     * @return 结果
     */
    public boolean isTypeGeometry(String column) {
        DBColumn dbColumn = this.column(column);
        if (dbColumn == null) {
            return false;
        }
        return dbColumn.supportGeometry();
    }
}
