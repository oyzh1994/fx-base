package cn.oyzh.fx.db.ui;

import cn.oyzh.fx.db.DBName;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.fx.plus.converter.SimpleStringConverter;

/**
 * 数据库名称选择框，用于展示并选择数据库对象名称
 *
 * @author oyzh
 * @since 2024-08-27
 */
public class DBNameComboBox extends FXComboBox<DBName> {

    @Override
    public void initNode() {
        this.setConverter(new SimpleStringConverter<>() {
            @Override
            public String toString(DBName object) {
                if (object != null) {
                    return object.getName();
                }
                return null;
            }
        });
        super.initNode();
    }
}
