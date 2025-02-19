package cn.oyzh.fx.plus.font;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.i18n.I18nHelper;

/**
 * 字体大小下拉框
 *
 * @author oyzh
 * @since 2024/04/05
 */
public class FontSizeComboBox extends FXComboBox<Integer> {

    {
        this.addItem(10);
        this.addItem(11);
        this.addItem(12);
        this.addItem(13);
        this.addItem(14);
        this.addItem(15);
        this.addItem(16);
        this.addItem(17);
        this.addItem(18);
        this.addItem(19);
        this.addItem(20);
        this.addItem(21);
        this.addItem(22);
        this.addItem(23);
        this.addItem(24);

        this.selectSize(null);
        this.setTipText(I18nHelper.fontSizeTip());
    }

    public void selectSize(Byte size) {
        if (size == null) {
            this.clearSelection();
        } else {
            super.selectObj(size.intValue());
        }
    }

    public Byte byteValue() {
        return this.getValue() == null ? null : this.getValue().byteValue();
    }
}
