package cn.oyzh.fx.plus.swing;

import cn.oyzh.common.system.OSUtil;
import cn.oyzh.fx.plus.theme.ThemeManager;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

/**
 * @author oyzh
 * @since 2025-08-04
 */
public class SwingUtil {

    // /**
    //  * 提交到任务队列运行函数
    //  *
    //  * @param func 函数
    //  */
    // public static void runTask(Runnable func) {
    //     SwingTask.getInstance().addTask(func);
    // }

    /**
     * 在awt的ui线程同步运行函数
     *
     * @param func 函数
     */
    public static void runWait(Runnable func) {
        try {
            if (SwingUtilities.isEventDispatchThread()) {
                func.run();
            } else if (OSUtil.isMacOS()) {
                new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() {
                        func.run();
                        return null;
                    }
                }.execute();
            } else {
                SwingUtilities.invokeAndWait(func);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 在awt的ui线程异步运行函数
     *
     * @param func 函数
     */
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

    /**
     * awt颜色转换为fx颜色
     *
     * @param color awt颜色
     * @return fx颜色
     */
    public static javafx.scene.paint.Color toFxColor(Color color) {
        return color == null ? null : javafx.scene.paint.Color.rgb(color.getRed(), color.getGreen(), color.getBlue(),
                color.getAlpha() / 255.0);
    }

    /**
     * 将 AWT Font 转换为 JavaFX Font
     *
     * @param awtFont AWT 字体对象
     * @return JavaFX 字体对象
     */
    public static javafx.scene.text.Font toFxFont(Font awtFont) {
        if (awtFont == null) {
            return null;
        }

        // 1. 获取字体家族名称
        String family = awtFont.getFamily();

        // 2. 获取字体大小（转换为 double 类型）
        double size = awtFont.getSize2D(); // getSize2D() 直接返回浮点型大小

        // 3. 转换字体样式（粗体和斜体）
        FontWeight weight = FontWeight.NORMAL;
        if (awtFont.isBold()) {
            weight = FontWeight.BOLD;
        }

        FontPosture posture = FontPosture.REGULAR;
        if (awtFont.isItalic()) {
            posture = FontPosture.ITALIC;
        }

        // 4. 创建并返回 JavaFX Font
        return javafx.scene.text.Font.font(family, weight, posture, size);
    }

    /**
     * 将 JavaFX Font 转换为 AWT Font
     *
     * @param fxFont JavaFX 字体对象
     * @return AWT 字体对象
     */
    public static Font fromFxFont(javafx.scene.text.Font fxFont) {
        if (fxFont == null) {
            return null;
        }
        // 1. 获取字体名称
        String family = fxFont.getFamily();
        // 2. 获取字体大小
        double size = fxFont.getSize();
        // 3. 转换字体样式（粗体、斜体）
        int style = fromFxStyle(fxFont.getStyle());
        // 4. 创建并返回 AWT Font
        return new Font(family, style, (int) Math.round(size));
    }

    /**
     * 从fx字体样式获取awt样式
     *
     * @param fxStyle fx字体样式
     * @return awt字体样式
     */
    public static int fromFxStyle(String fxStyle) {
        // 3. 转换字体样式（粗体、斜体）
        int style = Font.PLAIN; // 默认样式
        if (fxStyle.contains("bold")) {
            style |= Font.BOLD; // 叠加粗体样式
        }
        if (fxStyle.contains("italic")) {
            style |= Font.ITALIC; // 叠加斜体样式
        }
        return style;
    }

    /**
     * 从fx的字重获取awt字体
     *
     * @param family     字体
     * @param fontWeight 字重
     * @return awt字体名称
     */
    public static String fromFxWeight(String family, FontWeight fontWeight) {
        String adjustedFamily = family;
        // 处理字重（核心逻辑）
        switch (fontWeight) {
            case THIN:
                // 轻量字重：尝试使用带"THIN"后缀的字体变体
                adjustedFamily = family + " THIN";
                break;
            case EXTRA_LIGHT:
            case LIGHT:
                // 轻量字重：尝试使用带"Light"后缀的字体变体
                adjustedFamily = family + " Light";
                break;
            case SEMI_BOLD:
            case MEDIUM:
                // 半粗体：尝试使用带"SemiBold"后缀的字体变体
                adjustedFamily = family + " SemiBold";
                break;
            case EXTRA_BOLD:
            case BOLD:
                // 半粗体：尝试使用带"BOLD"后缀的字体变体
                adjustedFamily = family + " BOLD";
                break;
            case BLACK:
                // 特粗体：尝试使用带"Black"或"ExtraBold"后缀的字体变体
                adjustedFamily = family + " Black";
                break;
            default: // NORMAL
                adjustedFamily = family;
        }
        return adjustedFamily;
    }

    public static Color fromFxColor(javafx.scene.paint.Color color) {
        return color == null ? null : new Color(
                (int) Math.round(color.getRed() * 255),
                (int) Math.round(color.getGreen() * 255),
                (int) Math.round(color.getBlue() * 255),
                (int) Math.round(color.getOpacity() * 255));
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
        // 自定义滚动条UI
        scrollBar.setUI(new SwingScrollBarUI());
    }

}
