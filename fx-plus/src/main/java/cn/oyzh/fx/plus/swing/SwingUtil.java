package cn.oyzh.fx.plus.swing;

import cn.oyzh.fx.plus.theme.ThemeManager;

import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

/**
 * @author oyzh
 * @since 2025-08-04
 */
public class SwingUtil {

    public static void runWait(Runnable func) {
        try {
            if (SwingUtilities.isEventDispatchThread()) {
                func.run();
            } else {
                SwingUtilities.invokeAndWait(func);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void runLater(Runnable func) {
        try {
            if (SwingUtilities.isEventDispatchThread()) {
                func.run();
            } else {
                SwingUtilities.invokeLater(func);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static javafx.scene.paint.Color toFxColor(Color color) {
        return color == null ? null : javafx.scene.paint.Color.rgb(color.getRed(), color.getGreen(), color.getBlue(),
                color.getAlpha() / 255.0);
    }

    public static Color fromFxColor(javafx.scene.paint.Color color) {
        return color == null ? null : new Color(
                (int) Math.round(color.getRed() * 255),
                (int) Math.round(color.getGreen() * 255),
                (int) Math.round(color.getBlue() * 255),
                (int) Math.round(color.getOpacity() * 255));
    }

    /**
     * 反转AWT Color的颜色（计算RGB补色，保持透明度不变）
     *
     * @param color 原始颜色
     * @return 反转后的颜色
     */
    public static Color invert(Color color) {
        if (color == null) {
            return null;
        }
        // 计算RGB分量的补值（255 - 原始值）
        int red = 255 - color.getRed();
        int green = 255 - color.getGreen();
        int blue = 255 - color.getBlue();
        // 透明度保持不变
        int alpha = color.getAlpha();
        return new Color(red, green, blue, alpha);
    }

    /**
     * 应用主题的通用方法
     *
     * @param component 组件
     */
    public static void applyTheme(JComponent component) {
        Color bgColor = fromFxColor(ThemeManager.currentBackgroundColor());
        Color fgColor = fromFxColor(ThemeManager.currentForegroundColor());
        if (component instanceof JScrollPane node) {
            // 设置组件样式
            styleByScrollPane(node, bgColor, fgColor);
        } else if (component instanceof JTextArea node) {
            // 设置组件样式
            styleByTextArea(node, bgColor, fgColor);
        }
    }

    /**
     * 设置样式
     *
     * @param textArea 组件
     * @param bgColor  背景色
     * @param fgColor  前景色
     */
    private static void styleByTextArea(JTextArea textArea, Color bgColor, Color fgColor) {
        // 设置组件样式
        textArea.setForeground(fgColor);
        textArea.setBackground(bgColor);
        textArea.setCaretColor(fgColor);
    }

    /**
     * 设置样式
     *
     * @param scrollPane 组件
     * @param bgColor    背景色
     * @param fgColor    前景色
     */
    private static void styleByScrollPane(JScrollPane scrollPane, Color bgColor, Color fgColor) {
        // 设置组件样式
        scrollPane.setForeground(fgColor);
        scrollPane.setBackground(bgColor);
        // 设置头部样式
        Component view = scrollPane.getRowHeader().getView();
        view.setBackground(bgColor);
        view.setForeground(fgColor);
        // 设置滚动条样式
        styleByScrollbar(scrollPane.getVerticalScrollBar(), bgColor, fgColor);
        styleByScrollbar(scrollPane.getHorizontalScrollBar(), bgColor, fgColor);
    }

    /**
     * 设置样式
     *
     * @param scrollBar 组件
     * @param bgColor   背景色
     * @param fgColor   前景色
     */
    private static void styleByScrollbar(JScrollBar scrollBar, Color bgColor, Color fgColor) {
        // 设置组件样式
        scrollBar.setBackground(bgColor);
        scrollBar.setForeground(fgColor);
        scrollBar.setBorder(new Border() {
            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {

            }

            @Override
            public Insets getBorderInsets(Component c) {
                return null;
            }

            @Override
            public boolean isBorderOpaque() {
                return false;
            }
        });
        // 自定义滚动条UI
        scrollBar.setUI(new SwingScrollBarUI());
    }

}
