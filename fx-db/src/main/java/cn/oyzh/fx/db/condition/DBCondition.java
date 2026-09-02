package cn.oyzh.fx.db.condition;


/**
 * 条件
 *
 * @author oyzh
 * @since 2024/06/26
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

    public DBCondition() {

    }

    public DBCondition(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public DBCondition(String name, String value, boolean requireCondition) {
        this.name = name;
        this.value = value;
        this.requireCondition = requireCondition;
    }

    public Object wrapCondition() {
        return this.wrapCondition(null, null);
    }

    public Object wrapCondition(Object condition) {
        return this.wrapCondition(null, condition);
    }

    public Object wrapCondition(String columnName) {
        return this.wrapCondition(columnName, null);
    }

    public Object wrapCondition(String columnName, Object condition) {
        return null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isRequireCondition() {
        return requireCondition;
    }

    public void setRequireCondition(boolean requireCondition) {
        this.requireCondition = requireCondition;
    }
}
