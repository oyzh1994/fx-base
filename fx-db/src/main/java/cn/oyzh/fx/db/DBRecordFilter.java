package cn.oyzh.fx.db;

import cn.oyzh.fx.db.ui.DBJoinSymbolComboBox;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.tableview.TableViewUtil;

/**
 * 记录过滤条件
 *
 * @author oyzh
 * @since 2024/06/26
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

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getJoinSymbol() {
        return joinSymbol;
    }

    public void setJoinSymbol(String joinSymbol) {
        this.joinSymbol = joinSymbol;
    }
}
