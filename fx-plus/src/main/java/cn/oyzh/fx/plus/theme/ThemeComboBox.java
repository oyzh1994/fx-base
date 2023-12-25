package cn.oyzh.fx.plus.theme;

import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.plus.SimpleStringConverter;
import cn.oyzh.fx.plus.controls.combo.FlexComboBox;

/**
 * 主题下拉框
 *
 * @author oyzh
 * @since 2023/12/18
 */
public class ThemeComboBox extends FlexComboBox<ThemeType> {

    {
        this.addItems(ThemeType.values());
        this.setConverter(new SimpleStringConverter<>() {
            @Override
            public String toString(ThemeType o) {
                return o.getDesc();
            }
        });
    }

    /**
     * 选择主题
     *
     * @param themeName 主题名称
     */
    public void select(String themeName) {
        if (StrUtil.isEmpty(themeName)) {
            this.select(0);
        } else {
            try {
                ThemeType themeType = ThemeType.valueOf(themeName.toUpperCase());
                super.select(themeType);
            } catch (Exception ex) {
                ex.printStackTrace();
                this.select(0);
            }
        }
    }
}
