package cn.oyzh.fx.plus.util;

import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.Clipboard;

import java.awt.*;
import java.awt.datatransfer.StringSelection;

/**
 * 粘贴板工具类
 *
 * @author oyzh
 * @since 2023/11/22
 */
public class ClipboardUtil {

    /**
     * 复制到粘贴板
     *
     * @param content 内容
     * @return 结果
     */
    public static boolean copy(Object content) {
        if (content instanceof TextInputControl control) {
            return setString(control.getText());
        }
        if (content instanceof CharSequence sequence) {
            return setString(sequence.toString());
        }
        return false;
    }

    /**
     * 粘贴到组件
     *
     * @param node 组件
     * @return 结果
     */
    public static boolean paste(Object node) {
        return paste(node, null);
    }

    /**
     * 粘贴到组件
     *
     * @param node    组件
     * @param content 内容
     * @return 结果
     */
    public static boolean paste(Object node, Object content) {
        try {
            if (node instanceof TextInputControl control) {
                control.paste();
            }
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * 设置字符串到粘贴板
     *
     * @param content 内容
     * @return 结果
     */
    public static boolean setString(String content) {
        try {
            StringSelection stringSelection = new StringSelection(content);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, stringSelection);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 设置字符串到粘贴板，并提示
     *
     * @param content 内容
     * @return 结果
     */
    public static boolean setStringAndTip(String content) {
        try {
            StringSelection stringSelection = new StringSelection(content);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, stringSelection);
            MessageBox.okToast(I18nHelper.copySuccess());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            MessageBox.warn(I18nHelper.copyFail());
        }
        return false;
    }

    // /**
    //  * 设置字符串到粘贴板，并提示
    //  *
    //  * @param content 内容
    //  * @param tipText 提示标题
    //  * @return 结果
    //  */
    // @Deprecated
    // public static boolean setStringAndTip(String content, String tipText) {
    //     try {
    //         StringSelection stringSelection = new StringSelection(content);
    //         Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, stringSelection);
    //         MessageBox.okToast(I18nHelper.copySuccess());
    //         return true;
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //         MessageBox.warn(I18nHelper.copyFail());
    //     }
    //     return false;
    // }

    /**
     * 获取粘贴板字符串
     *
     * @return 结果
     */
    public static String getString() {
        try {
            // 获取系统剪贴板
            Clipboard clipboard = Clipboard.getSystemClipboard();
            // 判断剪贴板中是否有文本内容
            if (clipboard.hasString()) {
                // 获取文本内容
                return clipboard.getString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 粘贴板是否有字符串
     *
     * @return 结果
     */
    public static boolean hasString() {
        try {
            // 获取系统剪贴板
            Clipboard clipboard = Clipboard.getSystemClipboard();
            return clipboard.hasString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
