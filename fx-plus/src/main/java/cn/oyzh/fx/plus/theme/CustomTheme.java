package cn.oyzh.fx.plus.theme;

import atlantafx.base.theme.Theme;
import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.util.StringUtil;
import javafx.scene.paint.Color;

import java.io.File;
import java.util.Locale;

/**
 * 自定义主题
 *
 * @author oyzh
 * @since 2024/4/4
 */
public class CustomTheme implements Theme, ThemeStyle {

    /**
     * 主题风格
     */
    private ThemeStyle theme;

    /**
     * 强调色
     */
    private Color accentColor;

    /**
     * 背景色
     */
    private Color backgroundColor;

    /**
     * 前景色
     */
    private Color foregroundColor;

    /**
     * 主题文件路径
     */
    private String themePath;

    /**
     * 更新主题样
     */
    public void updateTheme(String themeName, String bgColor, String fgColor, String accentColor) {
        this.theme = Themes.getTheme(themeName);
        if (StringUtil.isNotEmpty(accentColor)) {
            this.accentColor = Color.valueOf(accentColor);
        } else {
            this.accentColor = null;
        }
        if (StringUtil.isNotEmpty(bgColor)) {
            this.backgroundColor = Color.valueOf(bgColor);
        } else {
            this.backgroundColor = null;
        }
        if (StringUtil.isNotEmpty(fgColor)) {
            this.foregroundColor = Color.valueOf(fgColor);
        } else {
            this.foregroundColor = null;
        }
        // 删除样式文件
        if (this.themePath != null) {
            FileUtil.del(this.themePath.replace("file:/", ""));
        }
        // 设置主题文件
//        this.themePath = "file:/" + ThemeUtil.updateThemeCss(this.theme, fgColor, bgColor, accentColor);
        try {
            // 更新主题文件
            String filePath = ThemeUtil.updateThemeCss(this.theme, fgColor, bgColor, accentColor);
            File file = new File(filePath);
            this.themePath = file.toURI().toURL().toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public String getName() {
        return "CUSTOM";
    }

    @Override
    public String getDesc(Locale locale) {
        if (locale == Locale.TRADITIONAL_CHINESE) {
            return "定制主題";
        } else if (locale == Locale.SIMPLIFIED_CHINESE) {
            return "定制主题";
        }
        return "Custom";
    }

    @Override
    public String getUserAgentStylesheet() {
        if (this.themePath == null) {
            return this.theme.getUserAgentStylesheet();
        }
        return this.themePath;
    }

    @Override
    public String getCompressedUserAgentStylesheet() {
        return this.getUserAgentStylesheet();
    }

    @Override
    public String getUserAgentStylesheetBSS() {
        return null;
    }

    @Override
    public boolean isDarkMode() {
        return this.theme.isDarkMode();
    }

    public ThemeStyle getTheme() {
        return theme;
    }

    @Override
    public Color getAccentColor() {
        return accentColor;
    }

    @Override
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    @Override
    public Color getForegroundColor() {
        return foregroundColor;
    }

    public String getThemePath() {
        return themePath;
    }
}
