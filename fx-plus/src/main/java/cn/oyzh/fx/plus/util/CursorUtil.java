package cn.oyzh.fx.plus.util;

import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.stage.Window;

/**
 * fx鼠标相关操作
 *
 * @author oyzh
 * @since 2022/3/8
 */
public class CursorUtil {

    /**
     * 等待鼠标样式
     *
     * @param stage 页面
     */
    public static void waitCursor(Window stage) {
        setCursor(stage, Cursor.WAIT);
    }

    /**
     * 小手鼠标样式
     *
     * @param stage 页面
     */
    public static void handCursor(Window stage) {
        setCursor(stage, Cursor.HAND);
    }

    /**
     * 默认鼠标样式
     *
     * @param stage 页面
     */
    public static void defaultCursor(Window stage) {
        setCursor(stage, Cursor.DEFAULT);
    }

    /**
     * 等待鼠标样式
     *
     * @param node 组件
     */
    public static void waitCursor(Node node) {
        setCursor(node, Cursor.WAIT);
    }

    /**
     * 小手鼠标样式
     *
     * @param node 组件
     */
    public static void handCursor(Node node) {
        setCursor(node, Cursor.HAND);
    }

    /**
     * 默认鼠标样式
     *
     * @param node 组件
     */
    public static void defaultCursor(Node node) {
        setCursor(node, Cursor.DEFAULT);
    }

    /**
     * 设置鼠标样式
     *
     * @param node   组件
     * @param cursor 鼠标样式
     */
    public static void setCursor(Node node, Cursor cursor) {
        if (node != null && node.getCursor() != cursor) {
            FXUtil.runWait(() -> node.setCursor(cursor));
        }
    }

    /**
     * 设置鼠标样式
     *
     * @param stage  页面
     * @param cursor 鼠标
     */
    public static void setCursor(Window stage, Cursor cursor) {
        if (stage != null && stage.getScene() != null) {
            CursorUtil.setCursor(stage.getScene().getRoot(), cursor);
        }
    }
}
