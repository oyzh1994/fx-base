package cn.oyzh.fx.plus.controls.combo;

import java.util.List;

/**
 * @author oyzh
 * @since 2022/2/7
 */
public class StringComboBox extends FlexComboBox<String> {

    public StringComboBox() {
        super();
    }

    public StringComboBox(List<String> list) {
        super();
        super.setItem(list);
    }
}
