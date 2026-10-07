package cn.oyzh.fx.db.data.ui;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.i18n.I18nHelper;

/**
 * 数据库数据字段分隔符下拉框，提供分号、逗号、空格等分隔符
 *
 * @author oyzh
 * @since 2024/09/04
 */
public class DBDataFieldSeparatorComboBox extends FXComboBox<String> {

    @Override
    public void initNode() {
        this.addItem(I18nHelper.semicolon() + "(;)");
        this.addItem(I18nHelper.comma() + "(,)");
        this.addItem(I18nHelper.space() + "( )");
        super.initNode();
    }

    /**
     * 获取当前选中项对应的字段分隔符
     *
     * @return 字段分隔符
     */
    public String value() {
        int itemIndex = this.getSelectedIndex();
        if (itemIndex == 0) {
            return ";";
        }
        if (itemIndex == 1) {
            return ",";
        }
        return " ";
    }
}
