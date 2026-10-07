package cn.oyzh.fx.db.condition.ui;

import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.condition.DBCondition;
import cn.oyzh.fx.db.condition.DBConditionManager;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.fx.plus.converter.SimpleStringConverter;

/**
 * 数据库条件下拉框，用于选择指定数据库方言下的查询条件
 *
 * @author oyzh
 * @since 2024/06/26
 */
public class DBConditionComboBox extends FXComboBox<DBCondition> {

    /**
     * 构造条件下拉框
     *
     * @param dialect 数据库方言
     */
    public DBConditionComboBox(DBDialect dialect) {
        this.setItem(DBConditionManager.conditions(dialect));
    }

    @Override
    public void initNode() {
        this.setConverter(new SimpleStringConverter<>() {
            @Override
            public String toString(DBCondition o) {
                if (o == null) {
                    return "";
                }
                return o.getName();
            }
        });
        super.initNode();
    }
}
