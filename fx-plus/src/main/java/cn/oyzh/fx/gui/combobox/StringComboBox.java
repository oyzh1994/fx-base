package cn.oyzh.fx.gui.combobox;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

import java.util.List;

/**
 * 字符串选择框
 *
 * @author oyzh
 * @since 2024-07-04
 */
public class StringComboBox extends FXComboBox<String> {

    /**
     * 构造字符串下拉框面板对象。
     */
    public StringComboBox() {
        super();
    }

    /**
     * 构造字符串下拉框面板对象。
     *
     * @param list 列表
     */
    public StringComboBox(List<String> list) {
        super();
        super.setItem(list);
    }
}
