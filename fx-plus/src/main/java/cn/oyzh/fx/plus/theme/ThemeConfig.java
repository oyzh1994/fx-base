package cn.oyzh.fx.plus.theme;


import cn.oyzh.fx.common.util.StringUtil;
import lombok.Data;


/**
 * 主题配置
 *
 * @author oyzh
 * @since 2024/04/04
 */
@Data
public class ThemeConfig {

    /**
     * 名称
     */
    private String name;

    /**
     * 前景色
     */
    private String fgColor;

    /**
     * 背景色
     */
    private String bgColor;

    /**
     * 强调色
     */
    private String accentColor;

    /**
     * 是否定制
     *
     * @return 结果
     */
    public boolean isCustom() {
        ThemeStyle style = Themes.getTheme(name);
        if (style == Themes.SYSTEM) {
            return false;
        }

        if (StringUtil.isEmpty(fgColor) || StringUtil.isEmpty(bgColor) || StringUtil.isEmpty(accentColor)) {
            return false;
        }
        if (!StringUtil.equalsIgnoreCase(fgColor, style.getForegroundColorHex())) {
            return true;
        }
        if (!StringUtil.equalsIgnoreCase(bgColor, style.getBackgroundColorHex())) {
            return true;
        }
        return !StringUtil.equalsIgnoreCase(accentColor, style.getAccentColorHex());
    }
}
