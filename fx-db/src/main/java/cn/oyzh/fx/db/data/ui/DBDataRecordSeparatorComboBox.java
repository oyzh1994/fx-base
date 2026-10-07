package cn.oyzh.fx.db.data.ui;

import cn.oyzh.common.system.OSUtil;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * 数据库数据记录分隔符下拉框，提供 CRLF、LF、CR 等换行符
 *
 * @author oyzh
 * @since 2024-09-04
 */
public class DBDataRecordSeparatorComboBox extends FXComboBox<String> {

    @Override
    public void initNode() {
        this.addItem("CRLF");
        this.addItem("LF");
        this.addItem("CR");
        if (OSUtil.isWindows()) {
            this.select(0);
        } else if (OSUtil.isLinux()) {
            this.select(1);
        } else if (OSUtil.isMacOS()) {
            this.select(1);
        }
        super.initNode();
    }

    /**
     * 获取当前选中项对应的记录分隔符
     *
     * @return 记录分隔符
     */
    public String value() {
        int itemIndex = this.getSelectedIndex();
        if (itemIndex == 0) {
            return "\r\n";
        }
        if (itemIndex == 1) {
            return "\n";
        }
        return "\r";
    }
}
