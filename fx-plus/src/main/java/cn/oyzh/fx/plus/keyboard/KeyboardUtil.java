package cn.oyzh.fx.plus.keyboard;

import cn.oyzh.common.system.OSUtil;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

import java.util.ArrayList;
import java.util.List;

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

    /**
     * 获取输入用的键盘码
     *
     * @return 列表
     */
    public static List<KeyCode> getInputCodes() {
        List<KeyCode> codes = new ArrayList<>();
        // 字母
        codes.add(KeyCode.A);
        codes.add(KeyCode.B);
        codes.add(KeyCode.C);
        codes.add(KeyCode.D);
        codes.add(KeyCode.E);
        codes.add(KeyCode.F);
        codes.add(KeyCode.G);
        codes.add(KeyCode.H);
        codes.add(KeyCode.I);
        codes.add(KeyCode.J);
        codes.add(KeyCode.K);
        codes.add(KeyCode.L);
        codes.add(KeyCode.M);
        codes.add(KeyCode.N);
        codes.add(KeyCode.O);
        codes.add(KeyCode.P);
        codes.add(KeyCode.Q);
        codes.add(KeyCode.R);
        codes.add(KeyCode.S);
        codes.add(KeyCode.T);
        codes.add(KeyCode.U);
        codes.add(KeyCode.V);
        codes.add(KeyCode.W);
        codes.add(KeyCode.X);
        codes.add(KeyCode.Y);
        codes.add(KeyCode.Z);
        // 数字
        codes.add(KeyCode.DIGIT0);
        codes.add(KeyCode.DIGIT1);
        codes.add(KeyCode.DIGIT2);
        codes.add(KeyCode.DIGIT3);
        codes.add(KeyCode.DIGIT4);
        codes.add(KeyCode.DIGIT5);
        codes.add(KeyCode.DIGIT6);
        codes.add(KeyCode.DIGIT7);
        codes.add(KeyCode.DIGIT8);
        codes.add(KeyCode.DIGIT9);
        // 小键盘数字
        codes.add(KeyCode.NUMPAD0);
        codes.add(KeyCode.NUMPAD1);
        codes.add(KeyCode.NUMPAD2);
        codes.add(KeyCode.NUMPAD3);
        codes.add(KeyCode.NUMPAD4);
        codes.add(KeyCode.NUMPAD5);
        codes.add(KeyCode.NUMPAD6);
        codes.add(KeyCode.NUMPAD7);
        codes.add(KeyCode.NUMPAD8);
        codes.add(KeyCode.NUMPAD9);
        // 空格
        codes.add(KeyCode.SPACE);
        // 小数点
        codes.add(KeyCode.DECIMAL);
        // ➕
        codes.add(KeyCode.PLUS);
        // ➖
        codes.add(KeyCode.SUBTRACT);
        // ✖️
        codes.add(KeyCode.MULTIPLY);
        // ➗
        codes.add(KeyCode.DIVIDE);
        // 逗号
        codes.add(KeyCode.COMMA);
        // 斜杆
        codes.add(KeyCode.SLASH);
        // 分号
        codes.add(KeyCode.SEMICOLON);
        // 等号
        codes.add(KeyCode.EQUALS);
        // 左括号
        codes.add(KeyCode.OPEN_BRACKET);
        // 右括号
        codes.add(KeyCode.CLOSE_BRACKET);
        // 反斜杠
        codes.add(KeyCode.BACK_SLASH);
        // 单引号
        codes.add(KeyCode.QUOTE);
        // 双引号
        codes.add(KeyCode.BACK_QUOTE);
        // 连字符
        codes.add(KeyCode.MINUS);
        return codes;
    }

    /**
     * 获取按键字符
     *
     * @param e 事件
     * @return 按键字符
     */
    public static char getKeyChar(KeyEvent e) {
        char keyChar;
        if (!e.getText().isEmpty()) {
            keyChar = e.getText().charAt(0);
        } else {
            keyChar = '\uffff';
        }
        return keyChar;
    }
}
