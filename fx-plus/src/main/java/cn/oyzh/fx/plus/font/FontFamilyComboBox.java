package cn.oyzh.fx.plus.font;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.text.Font;

/**
 * 字体名称下拉框
 *
 * @author oyzh
 * @since 2024/04/05
 */
public class FontFamilyComboBox extends FXComboBox<String> {

    {
        this.addItems(Font.getFamilies());
        this.select(null);
        this.setTipText(I18nHelper.fontNameTip());
    }

    public String getDefault() {
        return FontManager.defaultFont.getFamily();
    }

    /**
     * 选择字体
     *
     * @param fontFamily 字体名称
     */
    @Override
    public void select(String fontFamily) {
        if (StringUtil.isEmpty(fontFamily)) {
            super.select(this.getDefault());
        } else {
            try {
                super.select(fontFamily);
            } catch (Exception ex) {
                ex.printStackTrace();
                super.select(this.getDefault());
            }
        }
    }
}
