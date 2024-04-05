package cn.oyzh.fx.plus.font;

import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.plus.controls.combo.FlexComboBox;
import javafx.scene.text.Font;

/**
 * 字体名称下拉框
 *
 * @author oyzh
 * @since 2024/04/05
 */
public class FontNameComboBox extends FlexComboBox<String> {

    {
        this.addItems(Font.getFontNames());
        this.select(null);
    }

    /**
     * 选择字体
     *
     * @param fontName 字体名称
     */
    @Override
    public void select(String fontName) {
        if (StrUtil.isEmpty(fontName)) {
            super.select(FontManager.defaultFont.getName());
        } else {
            try {
                super.select(fontName);
            } catch (Exception ex) {
                ex.printStackTrace();
                this.select(0);
            }
        }
    }
}
