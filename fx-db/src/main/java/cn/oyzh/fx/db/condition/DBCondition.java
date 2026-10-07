package cn.oyzh.fx.db.condition;


/**
 * 数据库条件基类，封装条件的名称、值及是否为必需条件
 *
 * @author oyzh
 * @since 2024-06-26
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
     * 包装条件
     *
     * @return 包装后的条件对象
     */
    public Object wrapCondition() {
        return this.wrapCondition(null, null);
    }

    /**
     * 包装条件
     *
     * @param columnName 列名
     * @param condition  条件值
     * @return 包装后的条件对象
     */
    public Object wrapCondition(String columnName, Object condition) {
        return null;
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
}
