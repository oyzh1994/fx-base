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
        if (this instanceof Text node) {
            Font font1 = node.getFont();
            if (!FontUtil.isSameFont(font, font1)) {
                node.setFont(font);
            }
        } else if (this instanceof Labeled node) {
            Font font1 = node.getFont();
            if (!FontUtil.isSameFont(font, font1)) {
                node.setFont(font);
            }
        } else if (this instanceof TextInputControl node) {
            Font font1 = node.getFont();
            if (!FontUtil.isSameFont(font, font1)) {
                node.setFont(font);
            }
        } else if (this instanceof Node) {
            this.setFontSize(font.getSize());
            this.setFontFamily(font.getFamily());
        }
    }

    /**
     * 获取字体
     *
     * @return 字体
     */
    default Font getFont() {
        if (this instanceof Text node) {
            return node.getFont();
        }
        if (this instanceof Labeled node) {
            return node.getFont();
        }
        if (this instanceof TextInputControl node) {
            return node.getFont();
        }
        if (this instanceof Node) {
            return Font.font(this.getFontFamily(), this.getFontSize());
        }
        return Font.getDefault();
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
        if (this instanceof Text node) {
            node.setFont(FontUtil.newFontBySize(node.getFont(), fontSize));
        } else if (this instanceof Labeled node) {
            node.setFont(FontUtil.newFontBySize(node.getFont(), fontSize));
        } else if (this instanceof TextInputControl node) {
            node.setFont(FontUtil.newFontBySize(node.getFont(), fontSize));
        } else if (this instanceof Node node) {
            NodeUtil.replaceStyle(node, "-fx-font-size", fontSize);
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
        if (this instanceof Text node) {
            return node.getFont().getSize();
        }
        if (this instanceof Labeled node) {
            return node.getFont().getSize();
        }
        if (this instanceof TextInputControl node) {
            return node.getFont().getSize();
        }
        if (this instanceof Node node) {
            String size = NodeUtil.getStyle(node, "-fx-font-size");
            if (size != null) {
                return Double.parseDouble(size);
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
        if (this instanceof Text node) {
            node.setFont(FontUtil.newFontByFamily(node.getFont(), fontFamily));
        } else if (this instanceof Labeled node) {
            node.setFont(FontUtil.newFontByFamily(node.getFont(), fontFamily));
        } else if (this instanceof TextInputControl node) {
            node.setFont(FontUtil.newFontByFamily(node.getFont(), fontFamily));
        } else if (this instanceof Node node) {
            NodeUtil.replaceStyle(node, "-fx-font-family", fontFamily);
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
        if (this instanceof Text node) {
            return node.getFont().getFamily();
        }
        if (this instanceof Labeled node) {
            return node.getFont().getFamily();
        }
        if (this instanceof TextInputControl node) {
            return node.getFont().getFamily();
        }
        if (this instanceof Node node) {
            String family = NodeUtil.getStyle(node, "-fx-font-family");
            if (family != null) {
                return family;
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
        if (this instanceof Text node) {
            node.setFont(FontUtil.newFontByWeight(node.getFont(), fontWeight));
        } else if (this instanceof Labeled node) {
            node.setFont(FontUtil.newFontByWeight(node.getFont(), fontWeight));
        } else if (this instanceof TextInputControl node) {
            node.setFont(FontUtil.newFontByWeight(node.getFont(), fontWeight));
        } else if (this instanceof Node node) {
            NodeUtil.replaceStyle(node, "-fx-font-weight", fontWeight.getWeight());
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
        if (this instanceof Text node) {
            return FontWeight.findByName(node.getFont().getFamily());
        }
        if (this instanceof Labeled node) {
            return FontWeight.findByName(node.getFont().getFamily());
        }
        if (this instanceof TextInputControl node) {
            return FontWeight.findByName(node.getFont().getFamily());
        }
        if (this instanceof Node node) {
            String weight = NodeUtil.getStyle(node, "-fx-font-weight");
            if (weight != null) {
                return FontWeight.findByName(weight);
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
