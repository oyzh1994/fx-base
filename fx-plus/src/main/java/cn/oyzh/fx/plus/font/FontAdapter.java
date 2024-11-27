package cn.oyzh.fx.plus.font;

import cn.oyzh.fx.plus.adapter.PropAdapter;
import cn.oyzh.fx.plus.util.NodeUtil;
import javafx.scene.Node;
import javafx.scene.control.Labeled;
import javafx.scene.control.TextInputControl;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import lombok.NonNull;

/**
 * 字体组件适配器
 *
 * @author oyzh
 * @since 2023/04/14
 */
public interface FontAdapter extends PropAdapter {

    /**
     * 设置字体
     *
     * @param font 字体
     */
    default void setFont(Font font) {
        if (font == null) {
            return;
        }
        switch (this) {
            case Text node -> {
                Font font1 = node.getFont();
                if (!FontUtil.isSameFont(font, font1)) {
                    node.setFont(font);
                }
            }
            case Labeled node -> {
                Font font1 = node.getFont();
                if (!FontUtil.isSameFont(font, font1)) {
                    node.setFont(font);
                }
            }
            case TextInputControl node -> {
                Font font1 = node.getFont();
                if (!FontUtil.isSameFont(font, font1)) {
                    node.setFont(font);
                }
            }
            case Node node -> {
                this.setFontSize(font.getSize());
                this.setFontFamily(font.getFamily());
            }
            default -> {
            }
        }
    }

    /**
     * 获取字体
     *
     * @return 字体
     */
    default Font getFont() {
        return switch (this) {
            case Text node -> node.getFont();
            case Labeled node -> node.getFont();
            case TextInputControl node -> node.getFont();
            case Node node -> Font.font(this.getFontFamily(), this.getFontSize());
            default -> Font.getDefault();
        };
    }

    /**
     * 设置字体大小
     *
     * @param fontSize 字体大小
     */
    void setFontSize(double fontSize);

    /**
     * 设置字体实现
     *
     * @param fontSize 字体大小
     */
    default void fontSize(double fontSize) {
        synchronized (this) {
            switch (this) {
                case Text node -> node.setFont(FontUtil.newFontBySize(node.getFont(), fontSize));
                case Labeled node -> node.setFont(FontUtil.newFontBySize(node.getFont(), fontSize));
                case TextInputControl node -> node.setFont(FontUtil.newFontBySize(node.getFont(), fontSize));
                case Node node -> NodeUtil.replaceStyle(node, "-fx-font-size", fontSize);
                default -> {
                }
            }
        }
    }

    /**
     * 获取字体大小
     *
     * @return 字体大小
     */
    double getFontSize();

    /**
     * 获取字体大小实现
     *
     * @return 字体大小
     */
    default double fontSize() {
        switch (this) {
            case Text node -> {
                return node.getFont().getSize();
            }
            case Labeled node -> {
                return node.getFont().getSize();
            }
            case TextInputControl node -> {
                return node.getFont().getSize();
            }
            case Node node -> {
                String size = NodeUtil.getStyle(node, "-fx-font-size");
                if (size != null) {
                    return Double.parseDouble(size);
                }
            }
            default -> {
            }
        }
        return Font.getDefault().getSize();
    }

    /**
     * 设置字体类型
     *
     * @param fontFamily 字体类型
     */
    void setFontFamily(@NonNull String fontFamily);

    /**
     * 设置字体类型实现
     *
     * @param fontFamily 字体类型
     */
    default void fontFamily(String fontFamily) {
        if (fontFamily == null) {
            return;
        }
        switch (this) {
            case Text node -> node.setFont(FontUtil.newFontByFamily(node.getFont(), fontFamily));
            case Labeled node -> node.setFont(FontUtil.newFontByFamily(node.getFont(), fontFamily));
            case TextInputControl node -> node.setFont(FontUtil.newFontByFamily(node.getFont(), fontFamily));
            case Node node -> NodeUtil.replaceStyle(node, "-fx-font-family", fontFamily);
            default -> {
            }
        }
    }

    /**
     * 获取字体类型
     *
     * @return 字体类型
     */
    String getFontFamily();

    /**
     * 获取字体类型实现
     *
     * @return 字体类型
     */
    default String fontFamily() {
        switch (this) {
            case Text node -> {
                return node.getFont().getFamily();
            }
            case Labeled node -> {
                return node.getFont().getFamily();
            }
            case TextInputControl node -> {
                return node.getFont().getFamily();
            }
            case Node node -> {
                String family = NodeUtil.getStyle(node, "-fx-font-family");
                if (family != null) {
                    return family;
                }
            }
            default -> {
            }
        }
        return Font.getDefault().getFamily();
    }

    /**
     * 设置字体粗细
     *
     * @param fontWeight 字体粗细
     */
    void setFontWeight(FontWeight fontWeight);

    /**
     * 设置字体粗细实现
     *
     * @param fontWeight 字体粗细
     */
    default void fontWeight(FontWeight fontWeight) {
        if (fontWeight == null) {
            return;
        }
        switch (this) {
            case Text node -> node.setFont(FontUtil.newFontByWeight(node.getFont(), fontWeight));
            case Labeled node -> node.setFont(FontUtil.newFontByWeight(node.getFont(), fontWeight));
            case TextInputControl node -> node.setFont(FontUtil.newFontByWeight(node.getFont(), fontWeight));
            case Node node -> NodeUtil.replaceStyle(node, "-fx-font-weight", fontWeight.getWeight());
            default -> {
            }
        }
    }

    /**
     * 获取字体粗细
     *
     * @return 字体粗细
     */
    FontWeight getFontWeight();

    /**
     * 获取字体粗细实现
     *
     * @return 字体粗细
     */
    default FontWeight fontWeight() {
        switch (this) {
            case Text node -> {
                return FontWeight.findByName(node.getFont().getFamily());
            }
            case Labeled node -> {
                return FontWeight.findByName(node.getFont().getFamily());
            }
            case TextInputControl node -> {
                return FontWeight.findByName(node.getFont().getFamily());
            }
            case Node node -> {
                String weight = NodeUtil.getStyle(node, "-fx-font-weight");
                if (weight != null) {
                    return FontWeight.findByName(weight);
                }
            }
            default -> {
            }
        }
        return FontWeight.findByName(Font.getDefault().getStyle());
    }

    /**
     * 禁用字体
     */
    default void disableFont() {
        this.setProp("_enableFont", false);
    }

    /**
     * 启用字体
     */
    default void enableFont() {
        this.removeProp("_enableFont");
    }

    /**
     * 设置启用字体
     *
     * @param enableFont 启用字体
     */
    default void setEnableFont(boolean enableFont) {
        this.setProp("_enableFont", enableFont);
    }

    /**
     * 是否启用字体
     *
     * @return 结果
     */
    default boolean isEnableFont() {
        Boolean b = this.getProp("_enableFont");
        return b == null || b;
    }

    /**
     * 变更字体
     *
     * @param font 字体
     */
    default void changeFont(Font font) {
        if (this.isEnableFont() && font != null) {
            this.setFont(font);
        }
    }
}
