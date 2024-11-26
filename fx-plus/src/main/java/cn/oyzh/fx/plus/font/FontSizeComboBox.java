package cn.oyzh.fx.plus.font;

import cn.oyzh.fx.plus.controls.combo.FlexComboBox;
import cn.oyzh.i18n.I18nHelper;

/**
 * 字体大小下拉框
 *
 * @author oyzh
 * @since 2024/04/05
 */
public class FontSizeComboBox extends FlexComboBox<Byte> {

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

        this.selectSize(null);
        this.setTipText(I18nHelper.fontSizeTip());
    }

    public Byte getDefault() {
        return 10;
    }

    @Override
    public void select(Byte obj) {
        if (obj == null) {
            super.select(this.getDefault());
        } else {
            super.select(obj);
        }
    }

    public void selectSize(Byte size) {
        if (size == null) {
            this.clearSelection();
        } else {
            super.select(size);
        }
    }
}
