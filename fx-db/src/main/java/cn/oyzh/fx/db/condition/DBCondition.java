package cn.oyzh.fx.db.condition;


import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.util.DBUtil;

import java.util.Collection;

/**
 * 数据库条件基类，封装条件的名称、值及是否为必需条件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public abstract class DBCondition {

    /**
     * 名称
     */
    private String name;

    /**
     * 值
     */
    private String value;

    /**
     * 需要条件标志位
     */
    private boolean requireCondition = true;

    /**
     * 构造数据库条件对象。
     */
    public DBCondition() {

    }

    /**
     * 构造条件
     *
     * @param name  名称
     * @param value 值
     */
    public DBCondition(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * 构造条件
     *
     * @param name             名称
     * @param value            值
     * @param requireCondition 是否为必需条件
     */
    public DBCondition(String name, String value, boolean requireCondition) {
        this.name = name;
        this.value = value;
        this.requireCondition = requireCondition;
    }

    /**
     * 包装条件值
     *
     * @param value 条件值
     * @return 包装后的条件值
     */
    protected String wrapValue(Object value) {
        Object object = DBUtil.wrapData(value, this.getDialect());
        return object == null ? null : object.toString();
    }

    /**
     * 包装条件
     *
     * @return 包装后的条件对象
     */
    public Object wrapCondition() {
        return this.wrapCondition(null, null);
    }

    /**
     * 包装条件值
     *
     * @param condition 条件值
     * @return 包装后的条件值
     */
    public Object wrapCondition(Object condition) {
        return this.wrapCondition(null,condition);
    }

    /**
     * 包装条件
     *
     * @param columnName 列名
     * @param condition  条件值
     * @return 包装后的条件对象
     */
    public Object wrapCondition(String columnName, Object condition) {
        if (!this.isRequireCondition()) {
            return this.getValue();
        }
        String wrapped = this.wrapValue(condition);
        return wrapped == null ? null : this.getValue() + " " + wrapped;
    }

    /**
     * 包装between条件
     *
     * @param condition 条件
     * @return 结果
     */
    public String wrapBetweenCondition(Object condition) {
        Object first = null;
        Object second = null;
        if (condition instanceof Object[] values && values.length >= 2) {
            first = values[0];
            second = values[1];
        } else if (condition instanceof Collection<?> values && values.size() >= 2) {
            first = CollectionUtil.get(values, 0);
            second = CollectionUtil.get(values, 1);
        }
        if (first == null || second == null) {
            return null;
        }
        return this.wrapValue(first) + " AND " + this.wrapValue(second);
    }

    /**
     * 包装list条件
     *
     * @param condition 条件
     * @return 结果
     */
    public String wrapListCondition(Object condition) {
        if (!(condition instanceof String value) || value.isBlank()) {
            return null;
        }
        StringBuilder builder = new StringBuilder("(");
        String[] items = value.split(",");
        for (int index = 0; index < items.length; index++) {
            if (index > 0) {
                builder.append(", ");
            }
            builder.append(this.wrapValue(items[index].trim()));
        }
        return builder.append(')').toString();
    }

    /**
     * 获取名称。
     *
     * @return 名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置名称。
     *
     * @param name 名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取值。
     *
     * @return 值
     */
    public String getValue() {
        return value;
    }

    /**
     * 设置值。
     *
     * @param value 值
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * 是否必填条件。
     *
     * @return 必填条件
     */
    public boolean isRequireCondition() {
        return requireCondition;
    }

    /**
     * 设置必填条件。
     *
     * @param requireCondition 必填条件
     */
    public void setRequireCondition(boolean requireCondition) {
        this.requireCondition = requireCondition;
    }

    /**
     * 获取方言
     *
     * @return 方言
     */
    public abstract DBDialect getDialect();
}
