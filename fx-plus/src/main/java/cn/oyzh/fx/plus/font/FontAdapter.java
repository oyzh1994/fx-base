package cn.oyzh.fx.plus.font;

import cn.oyzh.fx.plus.adapter.PropAdapter;
import cn.oyzh.fx.plus.node.NodeUtil;
import javafx.scene.Node;
import javafx.scene.control.Labeled;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextInputControl;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.List;

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
        // 缓存字体
        font = FontManager.cacheFont(font);
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
        } else if (this instanceof TabPane tabPane) {
            List<Tab> tabs = tabPane.getTabs();
            for (Tab tab : tabs) {
                if (tab.getContent() instanceof FontAdapter adapter) {
                    adapter.setFont(font);
                }
            }
        } else if (this instanceof TextInputControl node) {
            Font font1 = node.getFont();
            if (!FontUtil.isSameFont(font, font1)) {
                node.setFont(font);
            }
        } else if (NodeUtil.isRichtextImport && this instanceof org.fxmisc.flowless.VirtualizedScrollPane<?> pane) {
            if (pane.getContent() instanceof FontAdapter adapter) {
                adapter.setFont(font);
            }
        } else if (this instanceof Node node) {
            this.setFontSize(font.getSize());
            this.setFontFamily(font.getFamily());
            this.setFontWeight(FontUtil.getWeight(font.getStyle()));
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
            case Node node -> Font.font(this.getFontFamily(), this.getFontWeight(), this.getFontSize());
            default -> Font.getDefault();
        };
    }

    /**
     * 设置字体大小
     *
     * @param fontSize 字体大小
     */
    default void setFontSize(double fontSize) {
        synchronized (this) {
            if (this instanceof Text node) {
                this.setFont(FontUtil.newFontBySize(node.getFont(), fontSize));
            } else if (this instanceof Labeled node) {
                this.setFont(FontUtil.newFontBySize(node.getFont(), fontSize));
            } else if (this instanceof TabPane node) {
                for (Tab tab : node.getTabs()) {
                    if (tab.getContent() instanceof FontAdapter adapter) {
                        adapter.setFontSize(fontSize);
                    }
                }
            } else if (this instanceof TextInputControl node) {
                this.setFont(FontUtil.newFontBySize(node.getFont(), fontSize));
            } else if (this instanceof Node node) {
                NodeUtil.replaceStyle(node, "-fx-font-size", fontSize);
            }
        }
    }

    /**
     * 获取字体大小
     *
     * @return 字体大小
     */
    default double getFontSize() {
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
    default void setFontFamily(String fontFamily) {
        if (fontFamily == null) {
            return;
        }
        switch (this) {
            case Text node -> this.setFont(FontUtil.newFontByFamily(node.getFont(), fontFamily));
            case Labeled node -> this.setFont(FontUtil.newFontByFamily(node.getFont(), fontFamily));
            case TextInputControl node -> this.setFont(FontUtil.newFontByFamily(node.getFont(), fontFamily));
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
    default String getFontFamily() {
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
            case  Tab tab-> {
                if (tab.getContent() instanceof FontAdapter adapter) {
                    return adapter.getFontFamily();
                }
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
    default void setFontWeight2(int fontWeight) {
        this.fontWeight(FontWeight.findByWeight(fontWeight));
    }

    /**
     * 设置字体粗细
     *
     * @param fontWeight 字体粗细
     */
    default void setFontWeight(FontWeight fontWeight) {
        this.fontWeight(fontWeight);
    }

    /**
     * 设置字体粗细实现
     *
     * @param fontWeight 字体粗细
     */
    private void fontWeight(FontWeight fontWeight) {
        if (fontWeight == null) {
            return;
        }
        switch (this) {
            case Text node -> this.setFont(FontUtil.newFontByWeight(node.getFont(), fontWeight));
            case Labeled node -> this.setFont(FontUtil.newFontByWeight(node.getFont(), fontWeight));
            case TextInputControl node -> this.setFont(FontUtil.newFontByWeight(node.getFont(), fontWeight));
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
    default FontWeight getFontWeight() {
        if (this instanceof Text node) {
            return FontUtil.getWeight(node.getFont().getStyle());
        }
        if (this instanceof Labeled node) {
            return FontUtil.getWeight(node.getFont().getStyle());
        }
        if (this instanceof TextInputControl node) {
            return FontUtil.getWeight(node.getFont().getStyle());
        }
        if (this instanceof Tab tab) {
            if (tab.getContent() instanceof FontAdapter adapter) {
                return adapter.getFontWeight();
            }
        }
        if (NodeUtil.isRichtextImport && this instanceof org.fxmisc.flowless.VirtualizedScrollPane<?> pane) {
            if (pane.getContent() instanceof FontAdapter adapter) {
                return adapter.getFontWeight();
            }
        }
        if (this instanceof Node node) {
            String weight = NodeUtil.getStyle(node, "-fx-font-weight");
            if (weight != null) {
                return FontUtil.getWeight(weight);
            }
        }
        return FontUtil.getWeight(Font.getDefault().getStyle());
    }

    String ENABLE_FONT_KEY = "enable:font";

    /**
     * 禁用字体
     */
    default void disableFont() {
        this.setProp(ENABLE_FONT_KEY, false);
    }

    /**
     * 启用字体
     */
    default void enableFont() {
        this.removeProp(ENABLE_FONT_KEY);
    }

    /**
     * 设置启用字体
     *
     * @param enableFont 启用字体
     */
    default void setEnableFont(boolean enableFont) {
        this.setProp(ENABLE_FONT_KEY, enableFont);
    }

    /**
     * 是否启用字体
     *
     * @return 结果
     */
    default boolean isEnableFont() {
        Boolean b = this.getProp(ENABLE_FONT_KEY);
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
