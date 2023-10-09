package cn.oyzh.fx.plus.util;

import cn.hutool.core.util.StrUtil;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

/**
 * 字符工具类
 *
 * @author oyzh
 * @since 2023048/24
 */
@Slf4j
@UtilityClass
public class FontUtil {

    /**
     * 是否相同字体
     *
     * @param family1 字体类型1
     * @param size1   字体大小1
     * @param family2 字体类型2
     * @param size2   字体大小1
     * @return 结果
     */
    public static boolean isSameFont(String family1, double size1, String family2, double size2) {
        if (!isSameFamily(family1, family2)) {
            return false;
        }
        return size1 == size2;
    }

    /**
     * 是否相同字体类型
     *
     * @param family1 字体类型1
     * @param family2 字体类型2
     * @return 结果
     */
    public static boolean isSameFamily(String family1, String family2) {
        if (Objects.equals(family1, family2)) {
            return true;
        }
        if (family1 != null && family2 != null) {
            return Objects.equals(family1.toLowerCase(), family2.toLowerCase());
        }
        return false;
    }

    /**
     * 计算字符宽度
     *
     * @param str 字符串
     * @return 字符宽度
     */
    public static double stringWidth(String str) {
        if (StrUtil.isBlank(str)) {
            return 0;
        }
        FontMetrics fontMetrics = fontMetrics();
        return fontMetrics.stringWidth(str);
    }

    /* 计算字体高度
     *
     * @param fontSize 字体大小
     * @return 字体高度
     */
    public static double calcFontHeight(int fontSize) {
        FontMetrics fontMetrics = fontMetrics(javafx.scene.text.Font.getDefault().getName(), fontSize);
        return fontMetrics.getHeight();
    }

    /* 计算字体高度
     *
     * @param fontName 字体名称
     * @param fontSize 字体大小
     * @return 字体高度
     */
    public static double calcFontHeight(@NonNull String fontName, int fontSize) {
        FontMetrics fontMetrics = fontMetrics(fontName, fontSize);
        return fontMetrics.getHeight();
    }

    /* 计算字体高度
     *
     * @param font 字体
     * @return 字体高度
     */
    public static double calcFontHeight(@NonNull javafx.scene.text.Font font) {
        FontMetrics fontMetrics = fontMetrics(font);
        return fontMetrics.getHeight();
    }

    /**
     * 计算字体宽高
     *
     * @return FontMetrics
     */
    public static FontMetrics fontMetrics() {
        javafx.scene.text.Font font = javafx.scene.text.Font.getDefault();
        return fontMetrics(font.getName(), (int) font.getSize());
    }

    /**
     * 计算字体宽高
     *
     * @param fontName 字体名称
     * @param fontSize 字体大小
     * @return FontMetrics
     */
    public static FontMetrics fontMetrics(@NonNull String fontName, int fontSize) {
        Font font = new Font(fontName, Font.PLAIN, fontSize);
        return new JLabel().getFontMetrics(font);
    }

    /**
     * 计算字体宽高
     *
     * @param font 字体
     * @return FontMetrics
     */
    public static FontMetrics fontMetrics(@NonNull javafx.scene.text.Font font) {
        return fontMetrics(font.getName(), (int) font.getSize());
    }


}
