package cn.oyzh.fx.db.data.ui;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * 数据库数据记录标签下拉框，用于选择记录标签的根形式
 *
 * @author oyzh
 * @since 2026-09-01
 */
public class DBDataRecordLabelComboBox extends FXComboBox<String> {

    @Override
    public void initNode() {
        this.addItem("(Root)");
        this.addItem("RECORDS");
        super.initNode();
    }

    /**
     * 是否使用根标签
     *
     * @return 结果
     */
    public boolean isRoot() {
        return this.getSelectedIndex() == 0;
    }
}
