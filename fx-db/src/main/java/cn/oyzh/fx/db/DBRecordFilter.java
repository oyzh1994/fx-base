package cn.oyzh.fx.db;

import cn.oyzh.fx.db.ui.DBJoinSymbolComboBox;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.tableview.TableViewUtil;

import java.util.List;

/**
 * 记录过滤条件
 *
 * @author oyzh
 * @since 2024-06-26
 */
public class DBRecordFilter {

    /**
     * 值
     */
    protected Object value;

    /**
     * 是否已启用
     */
    protected boolean enabled = true;

    /**
     * 连接符号
     */
    protected String joinSymbol;

    /**
     * 字段
     */
    protected DBColumn column;

    /**
     * 字段列表
     */
    protected List<? extends DBColumn> columns;

    /**
     * 获取启用组件
     *
     * @return 启用组件
     */
    public FXCheckBox getEnabledControl() {
        FXCheckBox checkBox = new FXCheckBox();
        checkBox.setSelected(this.enabled);
        checkBox.selectedChanged((observable, oldValue, newValue) -> this.enabled = newValue);
        TableViewUtil.selectRowOnMouseClicked(checkBox);
        return checkBox;
    }

    /**
     * 获取连接符组件
     *
     * @return 连接符组件
     */
    public DBJoinSymbolComboBox getJoinSymbolControl() {
        DBJoinSymbolComboBox comboBox = new DBJoinSymbolComboBox();
        comboBox.selectFirstIfNull(this.joinSymbol);
        comboBox.selectedItemChanged((observable, oldValue, newValue) -> this.joinSymbol = newValue);
        TableViewUtil.selectRowOnMouseClicked(comboBox);
        this.setJoinSymbol(comboBox.getSelectedItem());
        return comboBox;
    }

    /**
     * 获取值。
     *
     * @return 值
     */
    public Object getValue() {
        return value;
    }

    /**
     * 设置值。
     *
     * @param value 值
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * 是否启用。
     *
     * @return 启用
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 设置启用。
     *
     * @param enabled 是否启用
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 获取连接符号。
     *
     * @return 连接符号
     */
    public String getJoinSymbol() {
        return joinSymbol;
    }

    /**
     * 设置连接符号。
     *
     * @param joinSymbol 连接符
     */
    public void setJoinSymbol(String joinSymbol) {
        this.joinSymbol = joinSymbol;
    }

    /**
     * 获取列。
     *
     * @return 列
     */
    public DBColumn getColumn() {
        return column;
    }

    /**
     * 设置列。
     *
     * @param column 列
     */
    public void setColumn(DBColumn column) {
        this.column = column;
    }

    /**
     * 获取列集合。
     *
     * @return 列集合
     */
    public List<? extends DBColumn> getColumns() {
        return columns;
    }

    /**
     * 设置列集合。
     *
     * @param columns 列集合
     */
    public void setColumns(List<? extends DBColumn> columns) {
        this.columns = columns;
    }
}
