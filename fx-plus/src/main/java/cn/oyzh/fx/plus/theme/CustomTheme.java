package cn.oyzh.fx.plus.theme;

import atlantafx.base.theme.Theme;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 自定义主题
 *
 * @author oyzh
 * @since 2024/4/4
 */
@Slf4j
public class CustomTheme implements Theme, ThemeStyle {

    /**
     * 主题风格
     */
    private ThemeStyle theme;

    /**
     * 强调色
     */
    @Getter
    private Color accentColor;

    /**
     * 背景色
     */
    @Getter
    private Color backgroundColor;

    /**
     * 前景色
     */
    @Getter
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
        if (StrUtil.isNotEmpty(accentColor)) {
            this.accentColor = Color.valueOf(accentColor);
        } else {
            this.accentColor = null;
        }
        if (StrUtil.isNotEmpty(bgColor)) {
            this.backgroundColor = Color.valueOf(bgColor);
        } else {
            this.backgroundColor = null;
        }
        if (StrUtil.isNotEmpty(fgColor)) {
            this.foregroundColor = Color.valueOf(fgColor);
        } else {
            this.foregroundColor = null;
        }
        // 删除样式文件
        if (this.themePath != null) {
            FileUtil.del(this.themePath.replace("file:/", ""));
        }
        // 设置主题文件
        this.themePath = "file:/" + ThemeUtil.updateThemeCss(this.theme, fgColor, bgColor, accentColor);
    }

    @Override
    public String getName() {
        return "CUSTOM";
    }

    @Override
    public String getDesc() {
        return "定制主题";
    }

    @Override
    public String getUserAgentStylesheet() {
        if (this.themePath == null) {
            return this.theme.getUserAgentStylesheet();
        }
        return this.themePath;
    }

    @Override
    public String getUserAgentStylesheetBSS() {
        return null;
    }

    @Override
    public boolean isDarkMode() {
        return this.theme.isDarkMode();
    }
}
