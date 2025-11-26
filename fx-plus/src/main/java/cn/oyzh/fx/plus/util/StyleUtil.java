package cn.oyzh.fx.plus.util;

import cn.oyzh.common.util.ArrayUtil;
import cn.oyzh.common.util.StringUtil;
import javafx.css.Styleable;
import javafx.scene.Node;
import javafx.scene.control.Tab;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 样式工具类
 *
 * @author oyzh
 * @since 2023/4/4
 */
public class StyleUtil {

    /**
     * 连接样式文件
     *
     * @param cssList 样式列表
     * @return 连接后的样式地址
     */
    public static String join(String... cssList) {
        if (ArrayUtil.isNotEmpty(cssList)) {
            StringBuilder builder = new StringBuilder();
            for (String s : cssList) {
                builder.append(";").append(s);
            }
            return builder.substring(1);
        }
        return "";
    }

    /**
     * 分割样式文件
     *
     * @param cssList 样式列表
     * @return 分割后的样式列表
     */
    public static List<String> split(String... cssList) {
        if (ArrayUtil.isNotEmpty(cssList)) {
            List<String> list = new ArrayList<>(4);
            for (String s : cssList) {
                if (s.contains(";")) {
                    Collections.addAll(list, s.split(";"));
                } else {
                    list.add(s);
                }
            }
            return list;
        }
        return Collections.emptyList();
    }

    /**
     * 切换样式
     *
     * @param node       节点
     * @param styleClass 样式类
     */
    public static void toggleStyleClass(Styleable node, String styleClass) {
        if (node == null) {
            throw new NullPointerException("Node cannot be null!");
        }
        if (styleClass == null) {
            throw new NullPointerException("Style class cannot be null!");
        }

        int idx = node.getStyleClass().indexOf(styleClass);
        if (idx >= 0) {
            node.getStyleClass().remove(idx);
        } else {
            node.getStyleClass().add(styleClass);
        }
    }

    /**
     * 添加样式
     *
     * @param node       节点
     * @param styleClass 样式类
     * @param excludes   移除类
     */
    public static void addStyleClass(Styleable node, String styleClass, String... excludes) {
        if (node == null) {
            throw new NullPointerException("Node cannot be null!");
        }
        if (styleClass == null) {
            throw new NullPointerException("Style class cannot be null!");
        }

        if (excludes != null && excludes.length > 0) {
            node.getStyleClass().removeAll(excludes);
        }

        if (!node.getStyleClass().contains(styleClass)) {
            node.getStyleClass().add(styleClass);
        }
    }

    /**
     * 拼接样式
     *
     * @param node  节点
     * @param prop  属性
     * @param value 值
     */
    public static void appendStyle(Styleable node, String prop, String value) {
        if (node == null) {
            throw new NullPointerException("Node cannot be null!");
        }

        if (prop == null || prop.isBlank() || value == null || value.isBlank()) {
            System.err.printf("Ignoring invalid style: property = '%s', value = '%s'%n", prop, value);
            return;
        }

        var style = Objects.requireNonNullElse(node.getStyle(), "");
        if (!style.isEmpty() && !style.endsWith(";")) {
            style += ";";
        }
        style = style + prop.trim() + ":" + value.trim() + ";";
        setStyle(node, style);
    }

    /**
     * 移除样式
     *
     * @param node 节点
     * @param prop 属性
     */
    public static void removeStyle(Styleable node, String prop) {
        if (node == null) {
            throw new NullPointerException("Node cannot be null!");
        }
        var currentStyle = node.getStyle();
        if (currentStyle == null || currentStyle.isBlank()) {
            return;
        }

        if (prop == null || prop.isBlank()) {
            System.err.printf("Ignoring invalid property = '%s'%n", prop);
            return;
        }

        String[] stylePairs = currentStyle.split(";");
        var newStyle = new StringBuilder();

        for (var s : stylePairs) {
            String[] styleParts = s.split(":");
            if (!styleParts[0].trim().equals(prop)) {
                newStyle.append(s);
                newStyle.append(";");
            }
        }

        setStyle(newStyle, newStyle.toString());
    }

    /**
     * 设置样式
     *
     * @param node  节点
     * @param style 样式
     */
    public static void setStyle(Object node, String style) {
        if (node instanceof Node node1) {
            node1.setStyle(style);
        } else if (node instanceof Tab node1) {
            node1.setStyle(style);
        }
    }

    /**
     * 获取样式值
     *
     * @param node 节点
     * @param prop 属性
     * @return 样式值
     */
    public static String getStyle(Styleable node, String prop) {
        String currentStyle = node.getStyle();
        if (StringUtil.isNotBlank(currentStyle) && StringUtil.isNotBlank(prop)) {
            String[] stylePairs = currentStyle.split(";");
            for (String stylePair : stylePairs) {
                String[] styleParts = stylePair.split(":");
                if (styleParts[0].trim().equals(prop)) {
                    return styleParts[1];
                }
            }
        }
        return "";
    }

    /**
     * 替换样式
     *
     * @param node  节点
     * @param prop  属性
     * @param value 值
     */
    public static void replaceStyle(Styleable node, String prop, Object value) {
        StyleUtil.removeStyle(node, prop);
        StyleUtil.appendStyle(node, prop, value.toString());
    }

}
