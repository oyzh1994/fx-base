package cn.oyzh.fx.plus.theme;

import cn.oyzh.fx.common.log.JulLog;
import cn.oyzh.fx.common.util.ResourceUtil;
import cn.oyzh.fx.common.util.UUIDUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.common.util.FileUtil;
import javafx.scene.paint.Color;
import lombok.experimental.UtilityClass;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 主题工具类
 *
 * @author oyzh
 * @since 2024/4/3
 */
@UtilityClass
public class ThemeUtil {

    /**
     * 计算两个RGB颜色之间的欧几里得距离
     *
     * @param rgb1 颜色1
     * @param rgb2 颜色2
     * @return 结果
     */
    public static double distance(double[] rgb1, double[] rgb2) {
        if (rgb1.length != rgb2.length) {
            throw new IllegalArgumentException("RGB arrays must have the same length");
        }
        double sum = 0;
        for (int i = 0; i < rgb1.length; i++) {
            double diff = rgb1[i] - rgb2[i];
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }

    /**
     * 计算相关度
     *
     * @param color1 颜色1
     * @param color2 颜色2
     * @return 相关度
     */
    public static double calcCorr(Color color1, Color color2) {
        if (color1 == null || color2 == null) {
            return -1L;
        }
        double[] rgb1 = new double[]{color1.getRed(), color1.getGreen(), color1.getBlue()};
        double[] rgb2 = new double[]{color2.getRed(), color2.getGreen(), color2.getBlue()};
        return distance(rgb1, rgb2);
    }

    /**
     * 获取接近系统的主题
     *
     * @return 接近系统的主题风格
     */
    public static ThemeStyle getSystemNear() {
        ThemeStyle style = null;
        double corr = 0;
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
    public String updateThemeCss(ThemeStyle style, String fgColor, String bgColor, String accentColor) {
        // 读取资源
        URL url = ResourceUtil.getResource(style.getUserAgentStylesheet());
        JulLog.info("style url:{}", url);
        // 读取内容
        List<String> lines = FileUtil.readLines(url, StandardCharsets.UTF_8);
        // 替换颜色
        StringBuilder content = new StringBuilder();
        for (String s : lines) {
            if (s.trim().startsWith("-color-fg-default:")) {
                content.append("-color-fg-default:").append(fgColor).append(";");
            } else if (s.trim().startsWith("-color-bg-default:")) {
                content.append("-color-bg-default:").append(bgColor).append(";");
            } else if (s.trim().startsWith("-color-accent-fg:")) {
                content.append("-color-accent-fg:").append(accentColor).append(";");
            } else {
                content.append(s);
            }
        }
        // 生成文件名
        String path = FXUtil.getAppStorePath() + "theme_" + UUIDUtil.uuidSimple() + ".css";
        // 写入文件
        FileUtil.writeUtf8String(content.toString(), path);
        // 替换特殊符号
        while (path.contains("\\")) {
            path = path.replace("\\", "/");
        }
        return path;
    }
}
