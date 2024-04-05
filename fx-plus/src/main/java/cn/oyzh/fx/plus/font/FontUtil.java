package cn.oyzh.fx.plus.font;

import cn.hutool.core.util.StrUtil;
import javafx.event.EventTarget;
import javafx.scene.Node;
import javafx.scene.control.Labeled;
import javafx.scene.control.TextInputControl;
import javafx.scene.text.Text;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 字符工具类
 *
 * @author oyzh
 * @since 2023048/24
 */
@UtilityClass
public class FontUtil {

    /**
     * 是否相同字体
     *
     * @param font1 字体类型1
     * @param font2   字体类型2
     * @return 结果
     */
    public static boolean isSameFont(javafx.scene.text.Font font1, javafx.scene.text.Font font2) {
        if (!isSameFamily(font1.getFamily(), font2.getFamily())) {
            return false;
        }
        return font1.getSize() == font2.getSize();
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

    /**
     * 计算字符宽度
     *
     * @param str  字符串
     * @param font 字符
     * @return 字符宽度
     */
    public static double stringWidth(String str, javafx.scene.text.Font font) {
        if (StrUtil.isBlank(str)) {
            return 0;
        }
        FontMetrics fontMetrics = fontMetrics(font);
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
    public static FontMetrics fontMetrics(javafx.scene.text.Font font) {
        if (font == null) {
            font = javafx.scene.text.Font.getDefault();
        }
        return fontMetrics(font.getName(), (int) font.getSize());
    }

    /**
     * 获取字体
     *
     * @param target 目标
     * @return 字体
     */
    public static javafx.scene.text.Font getFont(EventTarget target) {
        if (target instanceof Text text) {
            return text.getFont();
        }
        if (target instanceof Labeled labeled) {
            return labeled.getFont();
        }
        if (target instanceof TextInputControl inputControl) {
            return inputControl.getFont();
        }
        if (target instanceof Node) {
            return javafx.scene.text.Font.font(getFontFamily(target), getFontSize(target));
        }
        return javafx.scene.text.Font.getDefault();
    }

    /**
     * 获取字体大小
     *
     * @param target 目标
     * @return 字体大小
     */
    public static double getFontSize(EventTarget target) {
        if (target instanceof Text text) {
            return text.getFont().getSize();
        }
        if (target instanceof Labeled labeled) {
            return labeled.getFont().getSize();
        }
        if (target instanceof TextInputControl inputControl) {
            return inputControl.getFont().getSize();
        }
        if (target instanceof Node node) {
            try {
                String style = node.getStyle();
                if (StrUtil.isNotBlank(style) && style.toLowerCase().contains("-fx-font-size")) {
                    List<String> list = StrUtil.split(style, ";");
                    Optional<String> fontSize = list.stream().filter(f -> f.trim().toLowerCase().contains("-fx-font-size")).findAny();
                    if (fontSize.isPresent()) {
                        String size = fontSize.get().toLowerCase().trim().replace("-fx-font-size", "");
                        size = size.replace(":", "").trim();
                        return Double.parseDouble(size);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return javafx.scene.text.Font.getDefault().getSize();
    }

    /**
     * 获取字体类型
     *
     * @param target 目标
     * @return 字体类型
     */
    public static String getFontFamily(EventTarget target) {
        if (target instanceof Text text) {
            return text.getFont().getFamily();
        }
        if (target instanceof Labeled labeled) {
            return labeled.getFont().getFamily();
        }
        if (target instanceof TextInputControl inputControl) {
            return inputControl.getFont().getFamily();
        }
        if (target instanceof Node node) {
            try {
                String style = node.getStyle();
                if (StrUtil.isNotBlank(style) && style.toLowerCase().contains("-fx-font-family")) {
                    List<String> list = StrUtil.split(style, ";");
                    Optional<String> fontSize = list.stream().filter(f -> f.trim().toLowerCase().contains("-fx-font-family")).findAny();
                    if (fontSize.isPresent()) {
                        String family = fontSize.get().toLowerCase().trim().replace("-fx-font-family", "");
                        family = family.replace(":", "").trim();
                        return family;
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return javafx.scene.text.Font.getDefault().getFamily();
    }
}
