package cn.oyzh.fx.plus.util;

import cn.oyzh.common.util.StringUtil;
import javafx.scene.paint.Color;

import java.util.List;


/**
 * 颜色工具类
 *
 * @author oyzh
 * @since 2024/08/17
 */
public class FXColorUtil {

    /**
     * 获取颜色16进制值
     *
     * @param color 颜色
     * @return 16进制值
     */
    public static String getColorHex(Color color) {
        if (color == null) {
            return null;
        }
        // 将归一化的RGB值缩放到0-255范围
        int scaledRed = (int) (color.getRed() * 255);
        int scaledGreen = (int) (color.getGreen() * 255);
        int scaledBlue = (int) (color.getBlue() * 255);

        // 将整数转换为16进制，并确保每个颜色分量都是两位数
        String hexRed = Integer.toHexString(scaledRed).toUpperCase();
        String hexGreen = Integer.toHexString(scaledGreen).toUpperCase();
        String hexBlue = Integer.toHexString(scaledBlue).toUpperCase();

        hexRed = hexRed.length() == 1 ? "0" + hexRed : hexRed;
        hexGreen = hexGreen.length() == 1 ? "0" + hexGreen : hexGreen;
        hexBlue = hexBlue.length() == 1 ? "0" + hexBlue : hexBlue;

        // 返回16进制颜色字符串
        return "#" + hexRed + hexGreen + hexBlue;
    }

    /**
     * 样式颜色转web颜色
     *
     * @param styleColor 颜色
     * @return web颜色
     */
    public static String styleColorToWebColor(String styleColor) {
        if (StringUtil.startWithIgnoreCase(styleColor, "-fx-fill:")) {
            styleColor = styleColor.substring(9).trim();
            if (styleColor.startsWith("#")) {
                styleColor = styleColor.substring(1).trim();
            }
            if (styleColor.endsWith(";")) {
                styleColor = styleColor.substring(0, styleColor.length() - 1);
            }
            return Color.valueOf(styleColor).toString();
        }
        return styleColor;
    }

    // 计算两个颜色之间的欧几里得距离
    public static double colorDistance(Color c1, Color c2) {
        double rDiff = c1.getRed() - c2.getRed();
        double gDiff = c1.getGreen() - c2.getGreen();
        double bDiff = c1.getBlue() - c2.getBlue();
        return Math.sqrt(rDiff * rDiff + gDiff * gDiff + bDiff * bDiff);
    }

    // 在颜色列表中找到与目标颜色最接近的颜色
    public static Color findClosestColor(Color target, List<Color> colorList) {
        Color closest = null;
        double minDistance = 0.35;
        for (Color color : colorList) {
            double distance = colorDistance(target, color);
            if (distance < minDistance) {
                minDistance = distance;
                closest = color;
            }
        }
        return closest;
    }
}
