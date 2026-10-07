package cn.oyzh.fx.db.ui;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * 连接符选择框，提供 AND、OR 两种条件连接符
 *
 * @author oyzh
 * @since 2024/1/26
 */
public class DBJoinSymbolComboBox extends FXComboBox<String> {

    @Override
    public void initNode() {
        this.addItem("AND");
        this.addItem("OR");
        super.initNode();
    }
}
