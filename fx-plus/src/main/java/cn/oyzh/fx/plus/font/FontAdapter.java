package cn.oyzh.fx.plus.font;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.adapter.PropAdapter;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

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
//         if (font == null) {
//             return;
//         }
//         // 缓存字体
//         font = FontManager.cacheFont(font);
//         if (this instanceof Text node) {
//             Font font1 = node.getFont();
//             if (!FontUtil.isSameFont(font, font1)) {
//                 node.setFont(font);
//             }
//         } else if (this instanceof Labeled node) {
//             Font font1 = node.getFont();
//             if (!FontUtil.isSameFont(font, font1)) {
//                 node.setFont(font);
//             }
// //        } else if (this instanceof TabPane node) {
// //            this.setFontSize(font.getSize());
// //            this.setFontFamily(font.getFamily());
// //            this.setFontWeight(FontUtil.getWeight(font.getStyle()));
// //            List<Tab> tabs = node.getTabs();
// //            for (Tab tab : tabs) {
// //                if (tab instanceof FontAdapter adapter) {
// //                    adapter.setFont(font);
// //                }
// //            }
//         } else if (this instanceof TextInputControl node) {
//             Font font1 = node.getFont();
//             if (!FontUtil.isSameFont(font, font1)) {
//                 node.setFont(font);
//             }
// //        } else if (this instanceof Tab node) {
// //            this.setFontSize(font.getSize());
// //            this.setFontFamily(font.getFamily());
// //            this.setFontWeight(FontUtil.getWeight(font.getStyle()));
// //            if (node.getContent() instanceof FontAdapter adapter) {
// //                adapter.setFont(font);
// //            }
//         } else if (this instanceof Styleable) {
//             this.setFontSize(font.getSize());
//             this.setFontFamily(font.getFamily());
//             this.setFontWeight(FontUtil.getWeight(font.getStyle()));
//         }
        FontUtil.setFont(this, font);
    }

    /**
     * 获取字体
     *
     * @return 字体
     */
    default Font getFont() {
        // if (this instanceof Text node) {
        //     return node.getFont();
        // }
        // if (this instanceof Labeled node) {
        //     return node.getFont();
        // }
        // if (this instanceof TextInputControl node) {
        //     return node.getFont();
        // }
        // if (this instanceof Styleable) {
        //     return Font.font(this.getFontFamily(), this.getFontWeight(), this.getFontSize());
        // }
        // return Font.getDefault();
        return FontUtil.getFont(this);
    }

    /**
     * 设置字体大小
     *
     * @param fontSize 字体大小
     */
    default void setFontSize(double fontSize) {
        // if (this instanceof Text node) {
        //     this.setFont(FontUtil.newFontBySize(node.getFont(), fontSize));
        // } else if (this instanceof Labeled node) {
        //     this.setFont(FontUtil.newFontBySize(node.getFont(), fontSize));
        // } else if (this instanceof TextInputControl node) {
        //     this.setFont(FontUtil.newFontBySize(node.getFont(), fontSize));
        // } else if (this instanceof TabPane node) {
        //     StyleUtil.replaceStyle(node, "-fx-font-size", fontSize);
        //     for (Tab tab : node.getTabs()) {
        //         if (tab instanceof FontAdapter adapter) {
        //             adapter.setFontSize(fontSize);
        //         }
        //     }
        // } else if (this instanceof Tab node) {
        //     StyleUtil.replaceStyle(node, "-fx-font-size", fontSize);
        //     if (node.getContent() instanceof FontAdapter adapter) {
        //         adapter.setFontSize(fontSize);
        //     }
        // } else if (this instanceof Styleable node) {
        //     StyleUtil.replaceStyle(node, "-fx-font-size", fontSize);
        // }
        FontUtil.setFontSize(this, fontSize);
    }

    /**
     * 获取字体大小
     *
     * @return 字体大小
     */
    default double getFontSize() {
        // if (this instanceof Text) {
        //     return this.getFont().getSize();
        // }
        // if (this instanceof Labeled) {
        //     return this.getFont().getSize();
        // }
        // if (this instanceof TextInputControl) {
        //     return this.getFont().getSize();
        // }
        // if (this instanceof Styleable node) {
        //     String size = StyleUtil.getStyle(node, "-fx-font-size");
        //     if (StringUtil.isNotBlank(size)) {
        //         return Double.parseDouble(size);
        //     }
        // }
        // return Font.getDefault().getSize();
        return FontUtil.getFontSize(this);
    }

    /**
     * 设置字体类型
     *
     * @param fontFamily 字体类型
     */
    default void setFontFamily(String fontFamily) {
        // if (fontFamily == null) {
        //     return;
        // }
        // if (this instanceof Text node) {
        //     this.setFont(FontUtil.newFontByFamily(node.getFont(), fontFamily));
        // } else if (this instanceof Labeled node) {
        //     this.setFont(FontUtil.newFontByFamily(node.getFont(), fontFamily));
        // } else if (this instanceof TextInputControl node) {
        //     this.setFont(FontUtil.newFontByFamily(node.getFont(), fontFamily));
        // } else if (this instanceof TabPane node) {
        //     fontFamily = "\"" + fontFamily + "\" !important";
        //     StyleUtil.replaceStyle(node, "-fx-font-family", fontFamily);
        //     List<Tab> tabs = node.getTabs();
        //     for (Tab tab : tabs) {
        //
        //             FontUtil.setFont();
        //     }
        // } else if (this instanceof Tab node) {
        //     fontFamily = "\"" + fontFamily + "\" !important";
        //     StyleUtil.replaceStyle(node, "-fx-font-family", fontFamily);
        //     // if (node.getContent() instanceof FontAdapter adapter) {
        //     //     adapter.setFontFamily(fontFamily);
        //     // }
        // } else if (this instanceof Styleable node) {
        //     StyleUtil.replaceStyle(node, "-fx-font-family", fontFamily);
        // }
        FontUtil.setFontFamily(this, fontFamily);
    }

    /**
     * 获取字体类型
     *
     * @return 字体类型
     */
    default String getFontFamily() {
        // if (this instanceof Text) {
        //     return this.getFont().getFamily();
        // }
        // if (this instanceof Labeled) {
        //     return this.getFont().getFamily();
        // }
        // if (this instanceof TextInputControl) {
        //     return this.getFont().getFamily();
        // }
        // if (this instanceof Styleable node) {
        //     String family = StyleUtil.getStyle(node, "-fx-font-family");
        //     if (family != null) {
        //         return family;
        //     }
        // }
        // return Font.getDefault().getFamily();
        return FontUtil.getFontFamily(this);
    }

    /**
     * 设置字体粗细
     *
     * @param fontWeight 字体粗细
     */
    default void setFontWeight2(int fontWeight) {
        this.setFontWeight(FontWeight.findByWeight(fontWeight));
    }

    /**
     * 设置字体粗细
     *
     * @param fontWeight 字体粗细
     */
    default void setFontWeight(FontWeight fontWeight) {
        // if (fontWeight == null) {
        //     return;
        // }
        // if (this instanceof Text node) {
        //     this.setFont(FontUtil.newFontByWeight(node.getFont(), fontWeight));
        // } else if (this instanceof Labeled node) {
        //     this.setFont(FontUtil.newFontByWeight(node.getFont(), fontWeight));
        // } else if (this instanceof TextInputControl node) {
        //     this.setFont(FontUtil.newFontByWeight(node.getFont(), fontWeight));
        // } else if (this instanceof TabPane node) {
        //     StyleUtil.replaceStyle(node, "-fx-font-weight", fontWeight.getWeight());
        //     List<Tab> tabs = node.getTabs();
        //     for (Tab tab : tabs) {
        //         if (tab instanceof FontAdapter adapter) {
        //             adapter.setFontWeight(fontWeight);
        //         }
        //     }
        // } else if (this instanceof Tab node) {
        //     StyleUtil.replaceStyle(node, "-fx-font-weight", fontWeight.getWeight());
        //     if (node.getContent() instanceof FontAdapter adapter) {
        //         adapter.setFontWeight(fontWeight);
        //     }
        // } else if (this instanceof Styleable node) {
        //     StyleUtil.replaceStyle(node, "-fx-font-weight", fontWeight.getWeight());
        // }
        FontUtil.setFontWeight(this, fontWeight);
    }

    /**
     * 获取字体粗细
     *
     * @return 字体粗细
     */
    default FontWeight getFontWeight() {
        // if (this instanceof Text) {
        //     return FontUtil.getWeight(this.getFont().getStyle());
        // }
        // if (this instanceof Labeled) {
        //     return FontUtil.getWeight(this.getFont().getStyle());
        // }
        // if (this instanceof TextInputControl) {
        //     return FontUtil.getWeight(this.getFont().getStyle());
        // }
        // if (this instanceof Styleable node) {
        //     String weight = StyleUtil.getStyle(node, "-fx-font-weight");
        //     if (weight != null) {
        //         return FontUtil.getWeight(weight);
        //     }
        // }
        // return FontUtil.getWeight(Font.getDefault().getStyle());
        return FontUtil.getFontWeight(this);
    }

    String ENABLE_FONT_KEY = "enable:font";

    String ENABLE_FONT_SIZE_KEY = "enable:font:size";

    String ENABLE_FONT_WEIGHT_KEY = "enable:font:weight";

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
     * 禁用字体大小
     */
    default void disableFontSize() {
        this.setProp(ENABLE_FONT_SIZE_KEY, false);
    }

    /**
     * 启用字体大小
     */
    default void enableFontSize() {
        this.removeProp(ENABLE_FONT_SIZE_KEY);
    }

    /**
     * 设置启用字体大小
     *
     * @param enableFontSize 启用字体大小
     */
    default void setEnableFontSize(boolean enableFontSize) {
        this.setProp(ENABLE_FONT_SIZE_KEY, enableFontSize);
    }

    /**
     * 是否启用字体大小
     *
     * @return 结果
     */
    default boolean isEnableFontSize() {
        Boolean b = this.getProp(ENABLE_FONT_SIZE_KEY);
        return b == null || b;
    }

    /**
     * 禁用字体重量
     */
    default void disableFontWeight() {
        this.setProp(ENABLE_FONT_WEIGHT_KEY, false);
    }

    /**
     * 启用字体重量
     */
    default void enableFontWeight() {
        this.removeProp(ENABLE_FONT_WEIGHT_KEY);
    }

    /**
     * 设置启用字体重量
     *
     * @param enableFontWeight 启用字体重量
     */
    default void setEnableFontWeight(boolean enableFontWeight) {
        this.setProp(ENABLE_FONT_WEIGHT_KEY, enableFontWeight);
    }

    /**
     * 是否启用字体重量
     *
     * @return 结果
     */
    default boolean isEnableFontWeight() {
        Boolean b = this.getProp(ENABLE_FONT_WEIGHT_KEY);
        return b == null || b;
    }

    /**
     * 变更字体
     *
     * @param font 字体
     */
    default void changeFont(Font font) {
        if (this.isEnableFont() && font != null) {
            Font font1 = this.getFont();
            if (font1 != null) {
                // 检查字重
                if (StringUtil.isNotBlank(font1.getStyle()) && !StringUtil.equals(font1.getStyle(), font.getStyle())) {
                    font = FontUtil.newFontByWeight(font, font1.getStyle());
                }
                // 检查字号
                if (!this.isEnableFontSize()) {
                    font = FontUtil.newFontBySize(font, font1.getSize());
                }
                // 检查字重
                if (!this.isEnableFontWeight()) {
                    font = FontUtil.newFontByWeight(font, font1.getStyle());
                }
            }
            this.setFont(font);
        }
    }
}
