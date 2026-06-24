package cn.oyzh.fx.plus.theme;

import cn.oyzh.common.SysConst;
import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.ResourceUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.common.util.UUIDUtil;
import javafx.scene.paint.Color;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 主题工具类
 *
 * @author oyzh
 * @since 2024/4/3
 */
public class ThemeUtil {

    /**
     * 计算相关度
     *
     * @param color1 颜色1
     * @param color2 颜色2
     * @return 相关度（欧几里得距离，越小越相似）
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
        String path = SysConst.storeDir() + "theme_" + UUIDUtil.uuidSimple() + ".css";
        // 写入文件
        FileUtil.writeUtf8String(content.toString(), path);
        // 替换特殊符号
        while (path.contains("\\")) {
            path = path.replace("\\", "/");
        }
        return path;
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
        ThemeStyle target;
        if (current.isDarkMode()) {
            if (current == Themes.PRIMER_DARK) {
                target = Themes.PRIMER_LIGHT;
            } else if (current == Themes.NORD_DARK) {
                target = Themes.NORD_LIGHT;
            } else if (current == Themes.CUPERTINO_DARK) {
                target = Themes.CUPERTINO_LIGHT;
            } else if (current == Themes.INTELLIJ_DARK) {
                target = Themes.INTELLIJ_LIGHT;
            } else if (current == Themes.VSCODE_DARK) {
                target = Themes.VSCODE_LIGHT;
            } else if (current == Themes.CYBERPUNK_DARK) {
                target = Themes.CYBERPUNK_LIGHT;
            } else if (current == Themes.LIQUID_GLASS_DARK) {
                target = Themes.LIQUID_GLASS_LIGHT;
            } else if (current == Themes.ANIME_WARM_DARK) {
                target = Themes.ANIME_WARM_LIGHT;
            } else if (current == Themes.BUSINESS_DARK) {
                target = Themes.BUSINESS_LIGHT;
            } else {
                target = Themes.PRIMER_LIGHT;
            }
        } else {
            if (current == Themes.PRIMER_LIGHT) {
                target = Themes.PRIMER_DARK;
            } else if (current == Themes.NORD_LIGHT) {
                target = Themes.NORD_DARK;
            } else if (current == Themes.CUPERTINO_LIGHT) {
                target = Themes.CUPERTINO_DARK;
            } else if (current == Themes.INTELLIJ_LIGHT) {
                target = Themes.INTELLIJ_DARK;
            } else if (current == Themes.VSCODE_LIGHT) {
                target = Themes.VSCODE_DARK;
            } else if (current == Themes.CYBERPUNK_LIGHT) {
                target = Themes.CYBERPUNK_DARK;
            } else if (current == Themes.LIQUID_GLASS_LIGHT) {
                target = Themes.LIQUID_GLASS_DARK;
            } else if (current == Themes.ANIME_WARM_LIGHT) {
                target = Themes.ANIME_WARM_DARK;
            } else if (current == Themes.BUSINESS_LIGHT) {
                target = Themes.BUSINESS_DARK;
            } else {
                target = Themes.PRIMER_DARK;
            }
        }
        return target;
    }
}
