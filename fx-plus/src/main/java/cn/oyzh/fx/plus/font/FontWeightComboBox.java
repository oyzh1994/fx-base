package cn.oyzh.fx.plus.font;

import cn.oyzh.fx.plus.SimpleStringConverter;
import cn.oyzh.fx.plus.controls.combo.FlexComboBox;
import cn.oyzh.fx.plus.i18n.I18nHelper;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;
import javafx.scene.text.FontWeight;

/**
 * 字体粗细下拉框
 *
 * @author oyzh
 * @since 2024/04/05
 */
public class FontWeightComboBox extends FlexComboBox<FontWeight> {

    {
        this.addItems(FontWeight.values());
        this.setTipText(I18nHelper.fontWeightTip());
        this.setConverter(new SimpleStringConverter<>() {
            @Override
            public String toString(FontWeight o) {
                return o.getWeight() + "";
            }
        });
        this.select(null);
    }

    public FontWeight getDefault() {
        return FontWeight.findByName(FontManager.defaultFont.getStyle());
    }

    @Override
    public void select(FontWeight obj) {
        if (obj == null) {
            super.select(this.getDefault());
        } else {
            super.select(obj);
        }
    }

    public int getWeight() {
        return this.getSelectedItem().getWeight();
    }

    public void selectWeight(Integer fontWeight) {
        if (fontWeight == null) {
            this.select(null);
        } else {
            this.select(FontWeight.findByWeight(fontWeight));
        }
    }
}
