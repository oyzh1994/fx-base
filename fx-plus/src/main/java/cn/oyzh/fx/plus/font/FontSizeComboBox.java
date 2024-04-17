package cn.oyzh.fx.plus.font;

import cn.oyzh.fx.plus.controls.combo.FlexComboBox;
import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * 字体大小下拉框
 *
 * @author oyzh
 * @since 2024/04/05
 */
public class FontSizeComboBox extends FlexComboBox<Integer> {

    {
        this.addItem(10);
        this.addItem(12);
        this.addItem(14);
        this.addItem(16);
        this.addItem(18);
        this.addItem(20);
        this.addItem(22);
        this.addItem(24);
        this.addItem(26);
        this.addItem(28);
        this.addItem(30);
        this.addItem(32);
        this.addItem(34);
        this.addItem(36);

        this.select(null);

        this.setTipText(BaseResourceBundle.getBaseString("base.fontSizeTip"));
    }

    public Integer getDefault() {
        return 10;
    }

    @Override
    public void select(Integer obj) {
        if (obj == null) {
            super.select(this.getDefault());
        } else {
            super.select(obj);
        }
    }
}
