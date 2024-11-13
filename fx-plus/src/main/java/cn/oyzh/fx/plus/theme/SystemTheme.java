package cn.oyzh.fx.plus.theme;

import atlantafx.base.theme.Theme;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.common.util.FileUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.application.ColorScheme;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.scene.paint.Color;

import java.util.Locale;

/**
 * 系统主题风格
 *
 * @author oyzh
 * @since 2024/4/3
 */
// @Slf4j
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
        // 获取相近的主题
        ThemeStyle nearTheme = ThemeUtil.getSystemNear();
        // 删除样式文件
        if (this.themePath != null) {
            FileUtil.del(this.themePath.replace("file:/", ""));
        }
        // 设置主题文件
        this.themePath = "file:/" + ThemeUtil.updateThemeCss(nearTheme, this.getForegroundColorHex(), this.getBackgroundColorHex(), this.getAccentColorHex());
    }

    @Override
    public String getName() {
        return "SYSTEM";
    }

    @Override
    public String getDesc(Locale locale) {
        if (locale == Locale.TRADITIONAL_CHINESE) {
            return "暗黑基礎";
        } else if (locale == Locale.SIMPLIFIED_CHINESE) {
            return "跟随系统";
        }
        return "System";
    }

    @Override
    public String getUserAgentStylesheet() {
        if (this.themePath == null) {
            this.updateThemeCss();
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
        JulLog.info("accentColor:{} bgColor:{} fgColor:{}", this.getAccentColorHex(), this.getBackgroundColorHex(), this.getForegroundColorHex());
        TaskManager.startDelay("changeTheme", () -> FXUtil.runLater(() -> {
            this.updateThemeCss();
            ThemeManager.apply(this);
        }), 1000);
    }
}
