package cn.oyzh.fx.gui.combobox;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

import java.util.List;

/**
 * @author oyzh
 * @since 2022/2/7
 */
public class StringComboBox extends FXComboBox<String> {

    public StringComboBox() {
        super();
    }

    public StringComboBox(List<String> list) {
        super();
        super.setItem(list);
    }
}
