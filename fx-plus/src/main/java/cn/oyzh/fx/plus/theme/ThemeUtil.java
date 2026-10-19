package cn.oyzh.fx.plus.theme;

import cn.oyzh.common.SysConst;
import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.ResourceUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.common.util.UUIDUtil;
import cn.oyzh.fx.plus.theme.custom.SystemTheme;
import javafx.scene.paint.Color;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * 主题工具类
 *
 * @author oyzh
 * @since 2024-04-03
 */
public class ThemeUtil {

    /**
     * 计算相关度
     *
     * @param color1 颜色1
     * @param color2 颜色2
     * @return 相关度（相似度，越大越相似；颜色为空时返回-1）
     */
    public static double calcCorr(Color color1, Color color2) {
        if (color1 == null || color2 == null) {
            return -1L;
        }
        // 1. 将 0-1 的 double 值转换为 0-255 的 int 值
        int r1 = (int) Math.round(color1.getRed() * 255);
        int g1 = (int) Math.round(color1.getGreen() * 255);
        int b1 = (int) Math.round(color1.getBlue() * 255);

        int r2 = (int) Math.round(color2.getRed() * 255);
        int g2 = (int) Math.round(color2.getGreen() * 255);
        int b2 = (int) Math.round(color2.getBlue() * 255);

        // 2. 计算各通道差值
        int redDiff = r1 - r2;
        int greenDiff = g1 - g2;
        int blueDiff = b1 - b2;

        // 3. 计算欧几里得距离
        double distance = Math.sqrt(redDiff * redDiff + greenDiff * greenDiff + blueDiff * blueDiff);

        // 4. 将距离映射为相似度 (距离最大为 sqrt(3 * 255^2) ≈ 441.67)
        double maxDistance = Math.sqrt(3 * 255 * 255);
        return 1 - (distance / maxDistance);
    }

    /**
     * 获取接近系统的主题
     *
     * @return 接近系统的主题风格
     */
    public static ThemeStyle getSystemNear() {
        ThemeStyle style = null;
        double corr = -1;
        for (ThemeStyle theme : Themes.themes()) {
            double corr1 = theme.corr(Themes.SYSTEM);
            if (corr1 > corr) {
                corr = corr1;
                style = theme;
            }
        }
        return style;
    }

    /**
     * 更新主题样式文件
     *
     * @param style       样式
     * @param fgColor     前景色
     * @param bgColor     背景色
     * @param accentColor 强调色
     */
    public static String updateThemeCss(ThemeStyle style, String fgColor, String bgColor, String accentColor) {
        // 读取资源
        URL url = ResourceUtil.getResource(style.getUserAgentStylesheet());
        if (JulLog.isInfoEnabled()) {
            JulLog.info("style url:{}", url);
        }
        // 读取内容
        List<String> lines = FileUtil.readLines(url, StandardCharsets.UTF_8);
        // 替换颜色
        StringBuilder content = new StringBuilder();
        for (String s : lines) {
            s = s.trim();
            if (s.startsWith("-color-fg-default:")) {
                content.append("-color-fg-default:").append(fgColor).append(";");
            } else if (s.startsWith("-color-bg-default:")) {
                content.append("-color-bg-default:").append(bgColor).append(";");
            } else if (s.startsWith("-color-accent-fg:")) {
                content.append("-color-accent-fg:").append(accentColor).append(";");
            } else if (StringUtil.isNotBlank(s)) {
                content.append(s);
            }
        }
        // 生成文件名
        Path path = Path.of(SysConst.storeDir(), "theme_" + UUIDUtil.uuidSimple() + ".css");
        // 写入文件
        FileUtil.writeUtf8String(content.toString(), path);
        //        // 替换特殊符号
        //        while (path.contains("\\")) {
        //            path = path.replace("\\", "/");
        //        }
        return path.toString();
    }

    /**
     * 获取相反的主题
     *
     * @param current 当前主题
     * @return 结果
     */
    public static ThemeStyle getInverseTheme(ThemeStyle current) {
        if (current instanceof SystemTheme theme) {
            return getInverseTheme(theme.getBaseTheme());
        }
        String currentKey = inverseThemeKey(current);
        for (ThemeStyle theme : Themes.themes()) {
            if (!Objects.equals(current, theme)
                    && theme.isDarkMode() != current.isDarkMode()
                    && currentKey.equals(inverseThemeKey(theme))) {
                return theme;
            }
        }
        return current.isDarkMode() ? Themes.PRIMER_LIGHT : Themes.PRIMER_DARK;
    }

    /**
     * 获取主题系列标识，忽略明暗差异。
     *
     * @param theme 主题
     * @return 系列标识
     */
    private static String inverseThemeKey(ThemeStyle theme) {
        String name = theme.getName();
        if (name == null) {
            return theme.getClass().getSimpleName().toLowerCase(Locale.ROOT);
        }
        String key = name.trim().toLowerCase(Locale.ROOT).replaceAll("[\\s_-]+", " ");
        if (key.equals("github light default") || key.equals("github soft dark")) {
            return "github default";
        }
        if (key.startsWith("github ") && key.contains("colorblind")) {
            return "github colorblind";
        }
        if (key.startsWith("github ") && key.contains("tritanopia")) {
            return "github tritanopia";
        }
        return key.replaceAll("\\b(?:light|dark)\\b", "mode");
    }
}
