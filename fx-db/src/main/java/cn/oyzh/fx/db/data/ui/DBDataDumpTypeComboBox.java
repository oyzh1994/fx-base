package cn.oyzh.fx.db.data.ui;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.i18n.I18nHelper;

/**
 * 数据库数据转储类型下拉框，用于选择转储数据与结构或仅转储结构
 *
 * @author oyzh
 * @since 2026-09-01
 */
public class DBDataDumpTypeComboBox extends FXComboBox<String> {

    @Override
    public void initNode() {
        this.addItem(I18nHelper.dataAndStructure());
        this.addItem(I18nHelper.structure());
        super.initNode();
    }

    /**
     * 是否转储完整数据（数据与结构）
     *
     * @return 结果
     */
    public boolean isFull() {
        return this.getSelectedIndex() == 0;
    }

}
