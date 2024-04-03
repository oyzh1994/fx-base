package cn.oyzh.fx.plus.theme;

import javafx.scene.paint.Color;
import lombok.experimental.UtilityClass;

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
}
