package cn.oyzh.fx.plus.font;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.fx.plus.converter.SimpleStringConverter;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.text.FontWeight;

/**
 * 字体粗细下拉框
 *
 * @author oyzh
 * @since 2024/04/05
 */
public class FontWeightComboBox extends FXComboBox<FontWeight> {

    /**
     * 获取默认字体粗细
     *
     * @return 默认字体粗细
     */
    public FontWeight getDefault() {
        return FontUtil.getWeight(FontManager.defaultFont.getStyle());
    }

    @Override
    public void select(FontWeight obj) {
        if (obj == null) {
            super.select(this.getDefault());
        } else {
            super.select(obj);
        }
    }

    /**
     * 选择字体粗细
     *
     * @param fontWeight 字体粗细值
     */
    public void selectWeight(Integer fontWeight) {
        if (fontWeight == null) {
            this.select(null);
        } else {
            this.select(FontWeight.findByWeight(fontWeight));
        }
    }

    /**
     * 获取当前选中字体粗细值
     *
     * @return 字体粗细值
     */
    public short getWeight() {
        return (short) this.getSelectedItem().getWeight();
    }

    @Override
    public void initNode() {
        super.initNode();
        this.addItems(FontWeight.values());
        this.setTipText(I18nHelper.fontWeightTip());
        this.setConverter(new SimpleStringConverter<>() {
            @Override
            public String toString(FontWeight o) {
                if (o != null) {
                    return o.getWeight() + "";
                }
                return "";
            }
        });
        this.select(null);
    }

    @Override
    public void destroy() {
        this.setConverter(null);
        super.destroy();
    }
}
