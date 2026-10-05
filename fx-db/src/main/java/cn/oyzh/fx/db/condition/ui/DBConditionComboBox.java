package cn.oyzh.fx.db.condition.ui;

import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.condition.DBCondition;
import cn.oyzh.fx.db.condition.DBConditionManager;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.fx.plus.converter.SimpleStringConverter;

/**
 * @author oyzh
 * @since 2024/06/26
 */
public class DBConditionComboBox extends FXComboBox<DBCondition> {

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
