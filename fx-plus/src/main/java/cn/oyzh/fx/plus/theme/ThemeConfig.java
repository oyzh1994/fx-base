package cn.oyzh.fx.plus.theme;


import cn.oyzh.common.util.StringUtil;


/**
 * 主题配置
 *
 * @author oyzh
 * @since 2024-04-04
 */
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

    /**
     * 获取名称。
     *
     * @return 名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置名称。
     *
     * @param name 名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取前景颜色。
     *
     * @return 前景颜色
     */
    public String getFgColor() {
        return fgColor;
    }

    /**
     * 设置前景颜色。
     *
     * @param fgColor 前景颜色
     */
    public void setFgColor(String fgColor) {
        this.fgColor = fgColor;
    }

    /**
     * 获取背景颜色。
     *
     * @return 背景颜色
     */
    public String getBgColor() {
        return bgColor;
    }

    /**
     * 设置背景颜色。
     *
     * @param bgColor 背景颜色
     */
    public void setBgColor(String bgColor) {
        this.bgColor = bgColor;
    }

    /**
     * 获取强调颜色。
     *
     * @return 强调颜色
     */
    public String getAccentColor() {
        return accentColor;
    }

    /**
     * 设置强调颜色。
     *
     * @param accentColor 强调颜色
     */
    public void setAccentColor(String accentColor) {
        this.accentColor = accentColor;
    }
}
