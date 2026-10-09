package cn.oyzh.fx.db.ui;

import cn.oyzh.fx.db.DBColumnFieldManager;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * db字段类型选择框
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class DBFiledTypeComboBox extends FXComboBox<String> {

    /**
     * 数据库方言
     */
    private DBDialect dialect;

    /**
     * 设置数据库方言并加载对应方言的字段类型
     *
     * @param dialect 数据库方言
     */
    public void setDialect(DBDialect dialect) {
        this.dialect = dialect;
        this.setItem(DBColumnFieldManager.fieldNames(dialect));
    }

    /**
     * 获取数据库方言
     *
     * @return 数据库方言
     */
    public DBDialect getDialect() {
        return dialect;
    }

    /**
     * 是否支持长度
     *
     * @return 结果
     */
    public boolean supportSize() {
        return DBColumnFieldManager.supportSize(this.dialect, this.getSelectedItem());
    }

    /**
     * 是否支持小数
     *
     * @return 结果
     */
    public boolean supportDigits() {
        return DBColumnFieldManager.supportDigits(this.dialect, this.getSelectedItem());
    }

    /**
     * 是否支持自动递增
     *
     * @return 结果
     */
    public boolean supportAutoIncrement() {
        return DBColumnFieldManager.supportAutoIncrement(this.dialect, this.getSelectedItem());
    }

    /**
     * 是否支持默认值
     *
     * @return 结果
     */
    public boolean supportDefaultValue() {
        return DBColumnFieldManager.supportDefaultValue(this.dialect, this.getSelectedItem());
    }

    /**
     * 是否支持当前时间戳
     *
     * @return 结果
     */
    public boolean supportTimestamp() {
        return DBColumnFieldManager.supportTimestamp(this.dialect, this.getSelectedItem());
    }

    /**
     * 是否支持json
     *
     * @return 结果
     */
    public boolean supportJson() {
        return DBColumnFieldManager.supportJson(this.dialect, this.getSelectedItem());
    }

    /**
     * 是否支持值
     *
     * @return 结果
     */
    public boolean supportValue() {
        return DBColumnFieldManager.supportValue(this.dialect, this.getSelectedItem());
    }

    /**
     * 是否支持字符集及排序
     *
     * @return 结果
     */
    public boolean supportCharset() {
        return DBColumnFieldManager.supportCharset(this.dialect, this.getSelectedItem());
    }

    /**
     * 获取示例值
     *
     * @return 示例值
     */
    public Object exampleValue() {
        return DBColumnFieldManager.exampleValue(this.dialect, this.getSelectedItem());
    }

    @Override
    public void select(String type) {
        if (type != null) {
            super.select(type.toUpperCase());
        } else {
            super.clearSelection();
        }
    }
}
