package cn.oyzh.fx.db;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

/**
 * 数据库记录
 *
 * @author oyzh
 * @since 2026-10-16
 */
public interface DBRecord {

    /**
     * 获取数据
     *
     * @param column 字段名
     * @return 数据
     */
    default Object getValue(String column) {
        DBRecordProperty property = this.getProperty(column);
        return property == null ? null : property.get();
    }

    /**
     * 获取原始数据
     *
     * @param column 字段名
     * @return 原始数据
     */
    default Object getOriginal(String column) {
        DBRecordProperty property = this.getProperty(column);
        return property == null ? null : property.getOriginal();
    }

    /**
     * 获取字段列表
     *
     * @return 字段列表
     */
    default Set<String> columns() {
        if (this.getProperties() == null) {
            return Collections.emptySet();
        }
        return this.getProperties().keySet();
    }

    /**
     * 抛弃变更
     */
    void discard() throws Exception;

    /**
     * 获取记录数据
     *
     * @return 结果
     */
    DBRecordData getRecordData();

    /**
     * 获取变更后记录数据
     *
     * @return 结果
     */
    default DBRecordData getChangedRecordData() {
        DBRecordData recordData = new DBRecordData();
        for (String column : this.columns()) {
            DBRecordProperty property =  this.getProperty(column);
            if (property != null && property.isChanged()) {
                recordData.put(property.getColumn(), property.get());
            }
        }
        return recordData;
    }

    /**
     * 获取原始记录数据
     *
     * @return 结果
     */
    default DBRecordData getOriginalRecordData() {
        DBRecordData recordData = new DBRecordData();
        for (String column : this.columns()) {
            DBRecordProperty property = this.getProperty(column);
            if (property != null) {
                Object val = property.getOriginal();
                recordData.put(property.getColumn(), val);
            }
        }
        return recordData;
    }


    /**
     * 是否可编辑
     *
     * @return 结果
     */
    boolean isEditable();

    /**
     * 设置可编辑
     *
     * @param editable 可编辑
     */
    void setEditable(boolean editable);

    /**
     * 获取记录属性集合
     *
     * @return 结果
     */
    HashMap<String, ? extends DBRecordProperty> getProperties();

    /**
     * 获取记录属性
     *
     * @param key 键
     * @return 属性
     */
    default DBRecordProperty getProperty(String key) {
        return this.getProperties().get(key);
    }

    /**
     * 是否存在记录属性
     *
     * @param recordProperty 记录属性
     * @return 属性
     */
    default boolean hasProperty(DBRecordProperty recordProperty) {
        return this.getProperties().containsValue(recordProperty);
    }

    /**
     * 移除记录属性
     *
     * @param key 键
     * @return 数据属性
     */
    default DBRecordProperty removeProperty(String key) {
        if (this.getProperties() != null) {
            return this.getProperties().remove(key);
        }
        return null;
    }

    /**
     * 清除数据
     */
    default void clearProperty() {
        if (this.getProperties() != null) {
            this.getProperties().clear();
        }
    }

    /**
     * 字段是否变更
     *
     * @param column 字段
     * @return 结果
     */
    default boolean isColumnChanged(String column) {
        DBRecordProperty property = this.getProperty(column);
        return property != null && property.isChanged();
    }

}
