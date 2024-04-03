package cn.oyzh.fx.plus.theme;

import atlantafx.base.theme.Theme;
import cn.hutool.core.io.FileUtil;
import cn.oyzh.fx.common.thread.TaskManager;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.util.ResourceUtil;
import javafx.application.ColorScheme;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.scene.paint.Color;

import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * @author oyzh
 * @since 2024/4/3
 */
public class SystemTheme implements Theme, ThemeStyle {

    /**
     * 颜色监听器
     */
    private final ChangeListener<Color> colorListener = (observable, oldValue, newValue) -> this.changeTheme();

    /**
     * 主题监听器
     */
    private final ChangeListener<ColorScheme> colorSchemeChangeListener = (observable, oldValue, newValue) -> this.changeTheme();

    /**
     * 是否跟随中
     */
    private transient boolean following;

    /**
     * 主题文件路径
     */
    private String themePath;

    /**
     * 初始化主题文件路径
     */
    private void initThemePath() {
        this.themePath = FXUtil.getAppStorePath() + "theme.css";
        while (this.themePath.contains("\\")) {
            this.themePath = themePath.replace("\\", "/");
        }
    }

    /**
     * 更新主题样式文件
     */
    public void updateThemeCss() {
        URL url;
        if (this.isDarkMode()) {
            url = ResourceUtil.getResource("/fx-plus/css/system-dark.css");
        } else {
            url = ResourceUtil.getResource("/fx-plus/css/system-light.css");
        }
        String content = FileUtil.readString(url, StandardCharsets.UTF_8);
        content = content.replace("${accent_color}", this.getAccentColorHex());
        content = content.replace("${bg_color}", this.getBackgroundColorHex());
        content = content.replace("${fg_color}", this.getForegroundColorHex());
        FileUtil.writeString(content, FXUtil.getAppStorePath() + "theme.css", StandardCharsets.UTF_8);
    }

    @Override
    public String getName() {
        return "SYSTEM";
    }

    @Override
    public String getDesc() {
        return "跟随系统";
    }

    @Override
    public String getUserAgentStylesheet() {
        if (this.themePath == null) {
            this.initThemePath();
        }
        return "file:/" + this.themePath;
    }

    @Override
    public String getUserAgentStylesheetBSS() {
        if (this.isDarkMode()) {
            return Themes.PRIMER_DARK.getUserAgentStylesheetBSS();
        }
        return Themes.PRIMER_LIGHT.getUserAgentStylesheetBSS();
    }

    @Override
    public boolean isDarkMode() {
        return Platform.getPreferences().getColorScheme() == ColorScheme.DARK;
    }

    @Override
    public Color getAccentColor() {
        return Platform.getPreferences().getAccentColor();
    }

    @Override
    public Color getForegroundColor() {
        return Platform.getPreferences().getForegroundColor();
    }

    @Override
    public Color getBackgroundColor() {
        return Platform.getPreferences().getBackgroundColor();
    }

    /**
     * 监听
     */
    public synchronized void listener() {
        if (!this.following) {
            this.following = true;
            Platform.getPreferences().accentColorProperty().addListener(this.colorListener);
            Platform.getPreferences().foregroundColorProperty().addListener(this.colorListener);
            Platform.getPreferences().backgroundColorProperty().addListener(this.colorListener);
            Platform.getPreferences().colorSchemeProperty().addListener(this.colorSchemeChangeListener);
        }
    }

    /**
     * 取消监听
     */
    public synchronized void unListener() {
        this.following = false;
        Platform.getPreferences().accentColorProperty().removeListener(this.colorListener);
        Platform.getPreferences().foregroundColorProperty().removeListener(this.colorListener);
        Platform.getPreferences().backgroundColorProperty().removeListener(this.colorListener);
        Platform.getPreferences().colorSchemeProperty().removeListener(this.colorSchemeChangeListener);
    }

    /**
     * 变更主题
     */
    protected void changeTheme() {
        this.updateThemeCss();
        TaskManager.startDelay("changeTheme", () -> FXUtil.runLater(() -> ThemeManager.changeTheme(this)), 1000);
    }
}
