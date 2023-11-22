package cn.oyzh.fx.plus.util;

import cn.oyzh.fx.plus.information.MessageBox;
import javafx.scene.input.Clipboard;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.awt.*;
import java.awt.datatransfer.StringSelection;

/**
 * 粘贴板工具类
 *
 * @author oyzh
 * @since 2023/11/22
 */
@UtilityClass
public class ClipboardUtil {

    /**
     * 设置字符串到粘贴板
     *
     * @param content 内容
     * @return 结果
     */
    public static boolean setString(@NonNull String content) {
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
     * @param content  内容
     * @param tipText 提示标题
     * @return 结果
     */
    public static boolean setStringAndTip(@NonNull String content, @NonNull String tipText) {
        try {
            StringSelection stringSelection = new StringSelection(content);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, stringSelection);
            MessageBox.okToast("已复制" + tipText + "到粘贴板");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            MessageBox.warn("复制" + tipText + "到粘贴板失败");
        }
        return false;
    }

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
}
