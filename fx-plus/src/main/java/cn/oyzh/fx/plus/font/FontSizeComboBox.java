package cn.oyzh.fx.plus.font;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.fx.plus.i18n.I18nSelectAdapter;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.i18n.I18nHelper;

import java.util.List;
import java.util.Locale;

/**
 * 字体大小下拉框
 *
 * @author oyzh
 * @since 2024/04/05
 */
public class FontSizeComboBox extends FXComboBox<Integer> implements I18nSelectAdapter<Integer> {

    {
        NodeManager.init(this);
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

    @Override
    public List<Integer> values(Locale locale) {
        List<Integer> list = List.of(10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25);
        this.setItem(list);
        return list;
    }
}
