package cn.oyzh.fx.plus.adapter;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.plus.util.FontUtil;
import javafx.scene.Node;
import javafx.scene.control.Labeled;
import javafx.scene.control.TextInputControl;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 字体组件适配器
 *
 * @author oyzh
 * @since 2023/04/14
 */
public interface FontAdapter {

    /**
     * 设置字体
     *
     * @param fontFamily 字体类型
     * @param fontSize   字体大小
     */
    default void setFont(@NonNull String fontFamily, double fontSize) {
        if (this instanceof Text text) {
            Font font = text.getFont();
            if (!FontUtil.isSameFont(fontFamily, fontSize, font.getFamily(), font.getSize())) {
                text.setFont(Font.font(fontFamily, fontSize));
            }
        } else if (this instanceof Labeled labeled) {
            Font font = labeled.getFont();
            if (!FontUtil.isSameFont(fontFamily, fontSize, font.getFamily(), font.getSize())) {
                labeled.setFont(Font.font(fontFamily, fontSize));
            }
        } else if (this instanceof TextInputControl inputControl) {
            Font font = inputControl.getFont();
            if (!FontUtil.isSameFont(fontFamily, fontSize, font.getFamily(), font.getSize())) {
                inputControl.setFont(Font.font(fontFamily, fontSize));
            }
        } else if (this instanceof Node) {
            this.setFontSize(fontSize);
            this.setFontFamily(fontFamily);
        }
    }

    /**
     * 获取字体
     *
     * @return 字体
     */
    default Font getFont() {
        if (this instanceof Text text) {
            return text.getFont();
        }
        if (this instanceof Labeled labeled) {
            return labeled.getFont();
        }
        if (this instanceof TextInputControl inputControl) {
            return inputControl.getFont();
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
        if (this instanceof Text text) {
            text.setFont(Font.font(this.getFontFamily(), fontSize));
        } else if (this instanceof Labeled labeled) {
            labeled.setFont(Font.font(this.getFontFamily(), fontSize));
        } else if (this instanceof TextInputControl inputControl) {
            inputControl.setFont(Font.font(this.getFontFamily(), fontSize));
        } else if (this instanceof Node node) {
            String style = node.getStyle() == null ? "" : node.getStyle();
            List<String> list = StrUtil.split(style, ";");
            list = list.stream().filter(f -> !f.trim().toLowerCase().contains("-fx-font-size")).collect(Collectors.toList());
            list.add("-fx-font-size: " + fontSize);
            node.setStyle(CollUtil.join(list, ";"));
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
        if (this instanceof Text text) {
            return text.getFont().getSize();
        }
        if (this instanceof Labeled labeled) {
            return labeled.getFont().getSize();
        }
        if (this instanceof TextInputControl inputControl) {
            return inputControl.getFont().getSize();
        }
        if (this instanceof Node node) {
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
    default void fontFamily(@NonNull String fontFamily) {
        if (this instanceof Text text) {
            text.setFont(Font.font(fontFamily, text.getFont().getSize()));
        } else if (this instanceof Labeled labeled) {
            labeled.setFont(Font.font(fontFamily, labeled.getFont().getSize()));
        } else if (this instanceof TextInputControl inputControl) {
            inputControl.setFont(Font.font(fontFamily, inputControl.getFont().getSize()));
        } else if (this instanceof Node node) {
            String style = node.getStyle() == null ? "" : node.getStyle();
            List<String> list = StrUtil.split(style, ";");
            list = list.stream().filter(f -> !f.trim().toLowerCase().contains("-fx-font-family")).collect(Collectors.toList());
            list.add("-fx-font-family: " + fontFamily);
            node.setStyle(CollUtil.join(list, ";"));
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
        if (this instanceof Text text) {
            return text.getFont().getFamily();
        }
        if (this instanceof Labeled labeled) {
            return labeled.getFont().getFamily();
        }
        if (this instanceof TextInputControl inputControl) {
            return inputControl.getFont().getFamily();
        }
        if (this instanceof Node node) {
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
        return Font.getDefault().getFamily();
    }
}
