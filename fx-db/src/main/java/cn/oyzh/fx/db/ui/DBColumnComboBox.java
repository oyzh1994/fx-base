package cn.oyzh.fx.db.ui;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.db.DBColumn;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.fx.plus.converter.SimpleStringConverter;

import java.util.List;

/**
 * 数据库字段选择框，用于展示并选择数据库字段
 *
 * @author oyzh
 * @since 2024-01-16
 */
public class DBColumnComboBox extends FXComboBox<DBColumn> {

    /**
     * 构造数据库列下拉框面板对象。
     */
    public DBColumnComboBox() {

    }

    /**
     * 构造数据库列下拉框面板对象。
     *
     * @param columns 列集合
     */
    public DBColumnComboBox(List<? extends DBColumn> columns) {
        this.setItem(columns);
    }

    /**
     * 根据字段名选中对应项
     *
     * @param colName 字段名
     */
    public void select(String colName) {
        for (DBColumn object : this.getItems()) {
            if (StringUtil.equalsIgnoreCase(colName, object.getName())) {
                this.select(object);
                break;
            }
        }
    }

    /**
     * 获取选中字段的名称
     *
     * @return 字段名称
     */
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
