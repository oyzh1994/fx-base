package cn.oyzh.fx.plus.theme;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.fx.plus.converter.SimpleStringConverter;
import cn.oyzh.i18n.I18nManager;
import javafx.scene.paint.Color;

/**
 * 主题下拉框
 *
 * @author oyzh
 * @since 2023/12/18
 */
public class ThemeComboBox extends FXComboBox<ThemeStyle> {

    /**
     * 选择主题
     *
     * @param themeName 主题名称
     */
    public void select(String themeName) {
        if (StringUtil.isEmpty(themeName)) {
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

    /**
     * 获取选中主题前景色
     *
     * @return 前景色
     */
    public Color getFgColor() {
        return this.getValue().getForegroundColor();
    }

    /**
     * 获取选中主题前景色16进制值
     *
     * @return 前景色16进制值
     */
    public String getFgColorHex() {
        return this.getValue().getForegroundColorHex();
    }

    /**
     * 获取选中主题背景色
     *
     * @return 背景色
     */
    public Color getBgColor() {
        return this.getValue().getBackgroundColor();
    }

    /**
     * 获取选中主题背景色16进制值
     *
     * @return 背景色16进制值
     */
    public String getBgColorHex() {
        return this.getValue().getBackgroundColorHex();
    }

    /**
     * 获取选中主题强调色
     *
     * @return 强调色
     */
    public Color getAccentColor() {
        return this.getValue().getAccentColor();
    }

    /**
     * 获取选中主题强调色16进制值
     *
     * @return 强调色16进制值
     */
    public String getAccentColorHex() {
        return this.getValue().getAccentColorHex();
    }

    @Override
    public void initNode() {
        this.addItems(Themes.allThemes());
        this.setConverter(new SimpleStringConverter<>() {
            @Override
            public String toString(ThemeStyle o) {
                if (o == null) {
                    return "";
                }
                return o.getDesc(I18nManager.currentLocale());
            }
        });
        super.initNode();
    }
}
