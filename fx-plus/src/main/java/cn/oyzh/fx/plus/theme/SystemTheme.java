package cn.oyzh.fx.plus.theme;

import atlantafx.base.theme.Theme;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.oyzh.fx.common.thread.TaskManager;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.util.ResourceUtil;
import javafx.application.ColorScheme;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.scene.paint.Color;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * @author oyzh
 * @since 2024/4/3
 */
@Slf4j
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
     * 更新主题样式文件
     */
    public void updateThemeCss() {
        URL url;
        if (this.isDarkMode()) {
            url = ResourceUtil.getResource("/fx-plus/css/system-dark.css");
        } else {
            url = ResourceUtil.getResource("/fx-plus/css/system-light.css");
        }
        // 替换颜色
        String content = FileUtil.readString(url, StandardCharsets.UTF_8);
        content = content.replace("${accent_color}", this.getAccentColorHex());
        content = content.replace("${bg_color}", this.getBackgroundColorHex());
        content = content.replace("${fg_color}", this.getForegroundColorHex());
        // 删除样式文件
        if (this.themePath != null) {
            FileUtil.del(this.themePath.replace("file:/", ""));
        }
        // 生成文件名
        String path = FXUtil.getAppStorePath() + "theme_" + UUID.fastUUID().toString(true) + ".css";
        // 写入文件
        FileUtil.writeUtf8String(content, path);
        // 替换特殊符号
        while (path.contains("\\")) {
            path = path.replace("\\", "/");
        }
        // 设置主题文件
        this.themePath = "file:/" + path;
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
            this.updateThemeCss();
        }
        return this.themePath;
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
        log.info("accentColor:{} bgColor:{} fgColor:{}", this.getAccentColorHex(), this.getBackgroundColorHex(), this.getForegroundColorHex());
        TaskManager.startDelay("changeTheme", () -> FXUtil.runLater(() -> {
            this.updateThemeCss();
            ThemeManager.changeTheme(this);
        }), 1000);
    }
}
