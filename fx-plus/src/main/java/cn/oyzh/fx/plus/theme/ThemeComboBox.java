package cn.oyzh.fx.plus.theme;

import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.plus.SimpleStringConverter;
import cn.oyzh.fx.plus.controls.combo.FlexComboBox;
import javafx.scene.paint.Color;

/**
 * 主题下拉框
 *
 * @author oyzh
 * @since 2023/12/18
 */
public class ThemeComboBox extends FlexComboBox<ThemeStyle> {

    {
        this.addItems(Themes.allThemes());
        this.setConverter(new SimpleStringConverter<>() {
            @Override
            public String toString(ThemeStyle o) {
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
                super.select(Themes.getTheme(themeName));
            } catch (Exception ex) {
                ex.printStackTrace();
                this.select(0);
            }
        }
    }

    /**
     * 获取主题名称
     *
     * @return 主题名称
     */
    public String name() {
        return this.getSelectedItem().getName();
    }

    /**
     * 是否系统主题
     *
     * @return 结果
     */
    public boolean isSystem() {
        return this.getSelectedItem() == Themes.SYSTEM;
    }

    public Color getFgColor() {
        return this.getValue().getForegroundColor();
    }

    public String getFgColorHex() {
        return this.getValue().getForegroundColorHex();
    }

    public Color getBgColor() {
        return this.getValue().getBackgroundColor();
    }

    public String getBgColorHex() {
        return this.getValue().getBackgroundColorHex();
    }

    public Color getAccentColor() {
        return this.getValue().getAccentColor();
    }

    public String getAccentColorHex() {
        return this.getValue().getAccentColorHex();
    }
}
