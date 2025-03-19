package cn.oyzh.fx.plus.keyboard;

import cn.oyzh.common.system.OSUtil;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

/**
 * 键盘按键事件
 *
 * @author oyzh
 * @since 2024/07/02
 */

public class KeyboardUtil {

    /**
     * 保存键组合
     */
    public static KeyCombination saveKeyCombination;

    /**
     * 全选键组合
     */
    public static KeyCombination selectAllKeyCombination;

    /**
     * 撤销键组合
     */
    public static KeyCombination redoKeyCombination;

    /**
     * 重做键组合
     */
    public static KeyCombination undoKeyCombination;

    /**
     * 剪切键组合
     */
    public static KeyCombination cutKeyCombination;

    /**
     * 粘贴键组合
     */
    public static KeyCombination pasteKeyCombination;

    /**
     * 复制键组合
     */
    public static KeyCombination copyKeyCombination;

    /**
     * 搜索键组合
     */
    public static KeyCombination searchKeyCombination;

    /**
     * 运行键组合
     */
    public static KeyCombination runKeyCombination;

    static {
        if (OSUtil.isMacOS()) {
            saveKeyCombination = new KeyCodeCombination(KeyCode.S, KeyCombination.META_DOWN);
            selectAllKeyCombination = new KeyCodeCombination(KeyCode.A, KeyCombination.META_DOWN);
            undoKeyCombination = new KeyCodeCombination(KeyCode.Z, KeyCombination.META_DOWN);
            redoKeyCombination = new KeyCodeCombination(KeyCode.Y, KeyCombination.META_DOWN);
            cutKeyCombination = new KeyCodeCombination(KeyCode.X, KeyCombination.META_DOWN);
            pasteKeyCombination = new KeyCodeCombination(KeyCode.V, KeyCombination.META_DOWN);
            copyKeyCombination = new KeyCodeCombination(KeyCode.C, KeyCombination.META_DOWN);
            searchKeyCombination = new KeyCodeCombination(KeyCode.F, KeyCombination.META_DOWN);
            runKeyCombination = new KeyCodeCombination(KeyCode.R, KeyCombination.META_DOWN);
        } else {
            saveKeyCombination = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);
            selectAllKeyCombination = new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN);
            undoKeyCombination = new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN);
            redoKeyCombination = new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN);
            cutKeyCombination = new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN);
            pasteKeyCombination = new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN);
            copyKeyCombination = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN);
            searchKeyCombination = new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN);
            runKeyCombination = new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN);
        }
    }

    /**
     * 是否按下ctrl s
     *
     * @param event 事件
     * @return 结果
     */
    public static boolean isCtrlS(KeyEvent event) {
        if (event == null) {
            return false;
        }
        return saveKeyCombination.match(event);
    }

    /**
     * 是否保存
     *
     * @param event 事件
     * @return 结果
     */
    public static boolean isSave(KeyEvent event) {
        if (event == null) {
            return false;
        }
        return saveKeyCombination.match(event);
    }

    /**
     * 是否剪切
     *
     * @param event 事件
     * @return 结果
     */
    public static boolean isCut(KeyEvent event) {
        if (event == null) {
            return false;
        }
        return cutKeyCombination.match(event);
    }

    /**
     * 是否复制
     *
     * @param event 事件
     * @return 结果
     */
    public static boolean isCopy(KeyEvent event) {
        if (event == null) {
            return false;
        }
        return copyKeyCombination.match(event);
    }

    /**
     * 是否全选
     *
     * @param event 事件
     * @return 结果
     */
    public static boolean isSelectAll(KeyEvent event) {
        if (event == null) {
            return false;
        }
        return selectAllKeyCombination.match(event);
    }

    /**
     * 是否撤销
     *
     * @param event 事件
     * @return 结果
     */
    public static boolean isUndo(KeyEvent event) {
        if (event == null) {
            return false;
        }
        return undoKeyCombination.match(event);
    }

    /**
     * 是否重做
     *
     * @param event 事件
     * @return 结果
     */
    public static boolean isRedo(KeyEvent event) {
        if (event == null) {
            return false;
        }
        return redoKeyCombination.match(event);
    }

    /**
     * 是否粘贴
     *
     * @param event 事件
     * @return 结果
     */
    public static boolean isPaste(KeyEvent event) {
        if (event == null) {
            return false;
        }
        return pasteKeyCombination.match(event);
    }

    /**
     * 是否按下ctrl slash
     *
     * @param event 事件
     * @return 结果
     */
    public static boolean isCtrlSlash(KeyEvent event) {
        if (event == null) {
            return false;
        }
        return event.isControlDown() && event.getCode() == KeyCode.SLASH;
    }

    /**
     * 是否按下ctrl r
     *
     * @param event 事件
     * @return 结果
     */
    public static boolean isCtrlR(KeyEvent event) {
        if (event == null) {
            return false;
        }
        return runKeyCombination.match(event);
    }

    /**
     * 是否按下enter
     *
     * @param event 事件
     * @return 结果
     */
    public static boolean isEnter(KeyEvent event) {
        if (event == null) {
            return false;
        }
        return event.getCode() == KeyCode.ENTER;
    }
}
