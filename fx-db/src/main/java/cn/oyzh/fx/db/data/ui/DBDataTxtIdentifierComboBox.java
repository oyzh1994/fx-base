package cn.oyzh.fx.db.data.ui;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * 数据库数据文本定界符下拉框，提供双引号与单引号选项
 *
 * @author oyzh
 * @since 2026-09-01
 */
public class DBDataTxtIdentifierComboBox extends FXComboBox<String> {

    @Override
    public void initNode() {
        this.addItem("\"");
        this.addItem("'");
    }
}
