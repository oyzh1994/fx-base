package cn.oyzh.fx.db.ui;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.db.DBColumn;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.fx.plus.converter.SimpleStringConverter;

import java.util.List;

/**
 * db字段类型选择框
 *
 * @author oyzh
 * @since 2024/01/16
 */
public class DBColumnComboBox extends FXComboBox<DBColumn> {

    public DBColumnComboBox() {

    }

    public DBColumnComboBox(List<? extends DBColumn> columns) {
        this.setItem(columns);
    }

    public void select(String colName) {
        for (DBColumn object : this.getItems()) {
            if (StringUtil.equalsIgnoreCase(colName, object.getName())) {
                this.select(object);
                break;
            }
        }
    }

    public String getColumnName() {
        return this.getSelectedItem().getName();
    }

    @Override
    public void initNode() {
        this.setConverter(new SimpleStringConverter<>() {
            @Override
            public String toString(DBColumn o) {
                if (o == null) {
                    return "";
                }
                return o.getName();
            }
        });
        super.initNode();
    }
}
