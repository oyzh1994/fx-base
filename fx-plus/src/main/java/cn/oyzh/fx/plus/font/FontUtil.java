package cn.oyzh.fx.plus.font;

import cn.oyzh.common.util.RegexUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.util.StyleUtil;
import javafx.css.Styleable;
import javafx.scene.control.Labeled;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextInputControl;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import javax.swing.JLabel;
import java.awt.Font;
import java.awt.FontMetrics;
import java.util.ArrayList;
import java.util.List;

/**
 * 字符工具类
 *
 * @author oyzh
 * @since 2023048/24
 */

public class FontUtil {

    /**
     * 创建一个新字体
     *
     * @param font 原字体
     * @param size 字体大小
     * @return 新字体
     */
    public static javafx.scene.text.Font newFontBySize(javafx.scene.text.Font font, double size) {
        FontWeight weight = getWeight(font.getStyle());
        String family = getTrueFamily(font.getFamily());
        javafx.scene.text.Font font1 = javafx.scene.text.Font.font(family, weight, size);
        return FontManager.cacheFont(font1);
    }

    /**
     * 创建一个新字体
     *
     * @param family 字体名称
     * @param size   字体大小
     * @return 新字体
     */
    public static javafx.scene.text.Font newFontBySize(String family, double size) {
        family = getTrueFamily(family);
        javafx.scene.text.Font font1 = javafx.scene.text.Font.font(family, size);
        return FontManager.cacheFont(font1);
    }

    /**
     * 创建一个新字体
     *
     * @param font   原字体
     * @param family 字体名称
     * @return 新字体
     */
    public static javafx.scene.text.Font newFontByFamily(javafx.scene.text.Font font, String family) {
        FontWeight weight = getWeight(font.getStyle());
        family = getTrueFamily(family);
        javafx.scene.text.Font font1 = javafx.scene.text.Font.font(family, weight, font.getSize());
        return FontManager.cacheFont(font1);
    }

    /**
     * 创建一个新字体
     *
     * @param font  原字体
     * @param style 字体粗细
     * @return 新字体
     */
    public static javafx.scene.text.Font newFontByWeight(javafx.scene.text.Font font, String style) {
        if (StringUtil.isBlank(style)) {
            return font;
        }
        FontWeight weight = getWeight(style);
        return newFontByWeight(font, weight);
    }

    /**
     * 创建一个新字体
     *
     * @param family  字体名称
     * @param posture 字体形式
     * @param size    字体大小
     * @return 新字体
     */
    public static javafx.scene.text.Font newFont(String family, FontPosture posture, double size) {
        family = getTrueFamily(family);
        javafx.scene.text.Font font1 = javafx.scene.text.Font.font(family, posture, size);
        return FontManager.cacheFont(font1);
    }

    /**
     * 创建一个新字体
     *
     * @param family 字体名称
     * @param weight 字体粗细
     * @param size   字体大小
     * @return 新字体
     */
    public static javafx.scene.text.Font newFont(String family, FontWeight weight, double size) {
        family = getTrueFamily(family);
        javafx.scene.text.Font font1 = javafx.scene.text.Font.font(family, weight, size);
        return FontManager.cacheFont(font1);
    }

    /**
     * 创建一个新字体
     *
     * @param family  字体名称
     * @param weight  字体粗细
     * @param posture 字体形式
     * @param size    字体大小
     * @return 新字体
     */
    public static javafx.scene.text.Font newFont(String family, FontWeight weight, FontPosture posture, double size) {
        family = getTrueFamily(family);
        javafx.scene.text.Font font1 = javafx.scene.text.Font.font(family, weight, posture, size);
        return FontManager.cacheFont(font1);
    }

    /**
     * 获取字体重量
     *
     * @param font 字体
     * @return 字体重量
     */
    public static FontWeight getWeight(javafx.scene.text.Font font) {
        return getWeight(font.getStyle());
    }

    /**
     * 获取字体重量
     *
     * @param style 字体样式
     * @return 字体重量
     */
    public static FontWeight getWeight(String style) {
        if (StringUtil.isNotBlank(style)) {
            for (String part : style.split(" ")) {
                FontWeight weight = FontWeight.findByName(part);
                if (weight != null) {
                    return weight;
                }
            }
        }
        return FontWeight.NORMAL;
    }

    /**
     * 获取字体重量2
     *
     * @param style 字体样式
     * @return 字体重量
     */
    public static int getWeight2(String style) {
        FontWeight fontWeight = getWeight(style);
        return fontWeight.getWeight();
    }

    /**
     * 创建一个新字体
     *
     * @param font   原字体
     * @param weight 字体粗细
     * @return 新字体
     */
    public static javafx.scene.text.Font newFontByWeight(javafx.scene.text.Font font, FontWeight weight) {
        String family = getTrueFamily(font.getFamily());
        javafx.scene.text.Font font1 = javafx.scene.text.Font.font(family, weight, font.getSize());
        return FontManager.cacheFont(font1);
    }

    /**
     * 是否相同字体
     *
     * @param font1 字体类型1
     * @param font2 字体类型2
     * @return 结果
     */
    public static boolean isSameFont(javafx.scene.text.Font font1, javafx.scene.text.Font font2) {
        if (!StringUtil.equalsIgnoreCase(font1.getFamily(), font2.getFamily())) {
            return false;
        }
        if (!StringUtil.equalsIgnoreCase(font1.getStyle(), font2.getStyle())) {
            return false;
        }
        return font1.getSize() == font2.getSize();
    }

    /**
     * 计算字符宽度
     *
     * @param str 字符串
     * @return 字符宽度
     */
    @Deprecated
    public static double stringWidth(String str) {
        if (StringUtil.isBlank(str)) {
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
        if (StringUtil.isBlank(str)) {
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
    public static double calcFontHeight(String fontName, int fontSize) {
        FontMetrics fontMetrics = fontMetrics(fontName, fontSize);
        return fontMetrics.getHeight();
    }

    /* 计算字体高度
     *
     * @param font 字体
     * @return 字体高度
     */
    public static double calcFontHeight(javafx.scene.text.Font font) {
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
    public static FontMetrics fontMetrics(String fontName, int fontSize) {
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
     * @param obj 对象
     * @return 结果
     */
    public static javafx.scene.text.Font getFont(Object obj) {
        if (obj instanceof Text text) {
            return text.getFont();
        }
        if (obj instanceof Labeled labeled) {
            return labeled.getFont();
        }
        if (obj instanceof TextInputControl inputControl) {
            return inputControl.getFont();
        }
        if (obj instanceof Styleable) {
            double size = getFontSize(obj);
            FontWeight weight = getFontWeight(obj);
            String family = getTrueFamily(getFontFamily(obj));
            return javafx.scene.text.Font.font(family, weight, size);
        }
        return javafx.scene.text.Font.getDefault();
    }

    /**
     * 设置字体
     *
     * @param obj  节点
     * @param font 字体
     */
    public static void setFont(Object obj, javafx.scene.text.Font font) {
        if (obj instanceof Labeled node) {
            node.setFont(font);
        } else if (obj instanceof Text node) {
            node.setFont(font);
        } else if (obj instanceof TextInputControl node) {
            node.setFont(font);
        } else if (obj instanceof TabPane node) {
            setFontSize(node, font.getSize());
            setFontFamily(node, font.getFamily());
            setFontWeight(node, getWeight(font.getStyle()));
            for (Tab tab : node.getTabs()) {
                setFont(tab, font);
            }
        } else if (obj instanceof Styleable node) {
            setFontSize(node, font.getSize());
            setFontFamily(node, font.getFamily());
            setFontWeight(node, getWeight(font.getStyle()));
        }
    }

    /**
     * 获取字体大小
     *
     * @param obj 对象
     * @return 结果
     */
    public static double getFontSize(Object obj) {
        if (obj instanceof Text text) {
            return text.getFont().getSize();
        }
        if (obj instanceof Labeled labeled) {
            return labeled.getFont().getSize();
        }
        if (obj instanceof TextInputControl inputControl) {
            return inputControl.getFont().getSize();
        }
        if (obj instanceof Styleable node) {
            // try {
            //     String style = node.getStyle();
            //     if (StringUtil.isNotBlank(style) && style.toLowerCase().contains("-fx-font-size")) {
            //         List<String> list = StringUtil.split(style, ";");
            //         Optional<String> fontSize = list.stream().filter(f -> f.trim().toLowerCase().contains("-fx-font-size")).findAny();
            //         if (fontSize.isPresent()) {
            //             String size = fontSize.get().toLowerCase().trim().replace("-fx-font-size", "");
            //             size = size.replace(":", "").trim();
            //             return Double.parseDouble(size);
            //         }
            //     }
            // } catch (Exception ex) {
            //     ex.printStackTrace();
            // }
            try {
                String size = StyleUtil.getStyle(node, "-fx-font-size");
                if (RegexUtil.isDecimal(size) || RegexUtil.isNumber(size)) {
                    return Double.parseDouble(size);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return javafx.scene.text.Font.getDefault().getSize();
    }

    /**
     * 设置字体大小
     *
     * @param obj      对象
     * @param fontSize 字体大小
     */
    public static void setFontSize(Object obj, double fontSize) {
        if (obj instanceof Text node) {
            setFont(obj, FontUtil.newFontBySize(node.getFont(), fontSize));
        } else if (obj instanceof Labeled node) {
            setFont(obj, FontUtil.newFontBySize(node.getFont(), fontSize));
        } else if (obj instanceof TextInputControl node) {
            setFont(obj, FontUtil.newFontBySize(node.getFont(), fontSize));
        } else if (obj instanceof TabPane node) {
            StyleUtil.replaceStyle(node, "-fx-font-size", fontSize + " !important");
            for (Tab tab : node.getTabs()) {
                setFontSize(tab, fontSize);
            }
        } else if (obj instanceof Styleable node) {
            StyleUtil.replaceStyle(node, "-fx-font-size", fontSize + " !important");
        }
    }

    /**
     * 获取字体类型
     *
     * @param obj 目标
     * @return 结果
     */
    public static String getFontFamily(Object obj) {
        if (obj instanceof Text text) {
            return text.getFont().getFamily();
        }
        if (obj instanceof Labeled labeled) {
            return labeled.getFont().getFamily();
        }
        if (obj instanceof TextInputControl inputControl) {
            return inputControl.getFont().getFamily();
        }
        if (obj instanceof Styleable node) {
            // try {
            //     String style = node.getStyle();
            //     if (StringUtil.isNotBlank(style) && style.toLowerCase().contains("-fx-font-family")) {
            //         List<String> list = StringUtil.split(style, ";");
            //         Optional<String> fontSize = list.stream().filter(f -> f.trim().toLowerCase().contains("-fx-font-family")).findAny();
            //         if (fontSize.isPresent()) {
            //             String family = fontSize.get().toLowerCase().trim().replace("-fx-font-family", "");
            //             family = family.replace(":", "").trim();
            //             return family;
            //         }
            //     }
            // } catch (Exception ex) {
            //     ex.printStackTrace();
            // }
            String family = StyleUtil.getStyle(node, "-fx-font-family");
            if (family != null) {
                return family;
            }
        }
        return javafx.scene.text.Font.getDefault().getFamily();
    }

    /**
     * 设置字体类型
     *
     * @param obj        对象
     * @param fontFamily 字体类型
     */
    public static void setFontFamily(Object obj, String fontFamily) {
        if (fontFamily == null) {
            return;
        }
        if (obj instanceof Text node) {
            node.setFont(FontUtil.newFontByFamily(node.getFont(), fontFamily));
        } else if (obj instanceof Labeled node) {
            node.setFont(FontUtil.newFontByFamily(node.getFont(), fontFamily));
        } else if (obj instanceof TextInputControl node) {
            node.setFont(FontUtil.newFontByFamily(node.getFont(), fontFamily));
        } else if (obj instanceof TabPane node) {
            StyleUtil.replaceStyle(node, "-fx-font-family", "'" + fontFamily + "' !important");
            for (Tab tab : node.getTabs()) {
                setFontFamily(tab, fontFamily);
            }
        } else if (obj instanceof Styleable node) {
            StyleUtil.replaceStyle(node, "-fx-font-family", "'" + fontFamily + "' !important");
        }
    }

    /**
     * 获取字体重量
     *
     * @param obj 对象
     * @return 结果
     */
    public static FontWeight getFontWeight(Object obj) {
        if (obj instanceof Text) {
            return FontUtil.getWeight(getFont(obj).getStyle());
        }
        if (obj instanceof Labeled) {
            return FontUtil.getWeight(getFont(obj).getStyle());
        }
        if (obj instanceof TextInputControl) {
            return FontUtil.getWeight(getFont(obj).getStyle());
        }
        if (obj instanceof Styleable node) {
            String weight = StyleUtil.getStyle(node, "-fx-font-weight");
            if (weight != null) {
                return FontUtil.getWeight(weight);
            }
        }
        return FontUtil.getWeight(javafx.scene.text.Font.getDefault().getStyle());
    }

    /**
     * 设置字体重量
     *
     * @param obj        对象
     * @param fontWeight 字体重量
     */
    public static void setFontWeight(Object obj, FontWeight fontWeight) {
        if (fontWeight == null) {
            return;
        }
        if (obj instanceof Text node) {
            setFont(obj, FontUtil.newFontByWeight(node.getFont(), fontWeight));
        } else if (obj instanceof Labeled node) {
            setFont(obj, FontUtil.newFontByWeight(node.getFont(), fontWeight));
        } else if (obj instanceof TextInputControl node) {
            setFont(obj, FontUtil.newFontByWeight(node.getFont(), fontWeight));
        } else if (obj instanceof TabPane node) {
            StyleUtil.replaceStyle(node, "-fx-font-weight", fontWeight.getWeight() + " !important");
            List<Tab> tabs = node.getTabs();
            for (Tab tab : tabs) {
                setFontWeight(tab, fontWeight);
            }
        } else if (obj instanceof Styleable node) {
            StyleUtil.replaceStyle(node, "-fx-font-weight", fontWeight.getWeight() + " !important");
        }
    }

    /**
     * 寻找真实的字体族
     *
     * @param family 字体族
     * @return 结果
     */
    public static String getTrueFamily(String family) {
        List<String> families = javafx.scene.text.Font.getFamilies();
        List<String> list = new ArrayList<>();
        for (String fm : families) {
            // 以这个开始，则加入列表
            if (StringUtil.startWithIgnoreCase(family, fm)) {
                list.add(fm);
            }
        }
        // 未找到
        if (list.isEmpty()) {
            return family;
        }
        // 单个直接返回
        if (list.size() == 1) {
            return list.getFirst();
        }
        // 寻找最接近的
        int size = 0;
        for (String fm : list) {
            if (fm.length() > size) {
                size = fm.length();
                family = fm;
            }
        }
        return family;
    }


    /**
     * 计算字符宽度
     *
     * @param str  字符串
     * @return 字符宽度
     */
    public static double textWidth(String str) {
        return textWidth(str,javafx.scene.text.Font.getDefault());
    }

    /**
     * 计算字符宽度
     *
     * @param str  字符串
     * @param font 字体
     * @return 字符宽度
     */
    public static double textWidth(String str, javafx.scene.text.Font font) {
        // 创建一个临时的Text对象来测量
        Text text = new Text(str);
        if (font != null) {
            // 应用可能的字体（这里使用默认系统字体，更精确的话需获取实际字体）
            text.setFont(font);
        }
        return text.getLayoutBounds().getWidth();
    }


}
