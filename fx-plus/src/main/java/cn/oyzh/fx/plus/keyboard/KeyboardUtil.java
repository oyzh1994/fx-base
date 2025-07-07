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
     * 保存快捷键
     */
    public static KeyCombination save_keyCombination;

    /**
     * 全选快捷键
     */
    public static KeyCombination selectAll_keyCombination;

    /**
     * 撤销快捷键
     */
    public static KeyCombination redo_keyCombination;

    /**
     * 重做快捷键
     */
    public static KeyCombination undo_keyCombination;

    /**
     * 剪切快捷键
     */
    public static KeyCombination cut_keyCombination;

    /**
     * 粘贴快捷键
     */
    public static KeyCombination paste_keyCombination;

    /**
     * 复制快捷键
     */
    public static KeyCombination copy_keyCombination;

    /**
     * 搜索快捷键
     */
    public static KeyCombination search_keyCombination;

    /**
     * 运行快捷键
     */
    public static KeyCombination run_keyCombination;

    /**
     * 应用快捷键
     */
    public final static KeyCombination apply_keyCombination;

    /**
     * 停止快捷键
     */
    public final static KeyCombination stop_keyCombination;

    /**
     * 重启快捷键
     */
    public final static KeyCombination restart_keyCombination = new KeyCodeCombination(KeyCode.R, KeyCombination.SHIFT_DOWN);

    /**
     * 删除快捷键
     */
    public final static KeyCombination delete_keyCombination = new KeyCodeCombination(KeyCode.DELETE);

    /**
     * 重命名快捷键
     */
    public final static KeyCombination rename_keyCombination = new KeyCodeCombination(KeyCode.F2);

    /**
     * 刷新快捷键
     */
    public final static KeyCombination refresh_keyCombination = new KeyCodeCombination(KeyCode.F5);

    /**
     * 信息快捷键
     */
    public final static KeyCombination info_keyCombination = new KeyCodeCombination(KeyCode.I, KeyCombination.SHIFT_DOWN);

    /**
     * 编辑快捷键
     */
    public static final KeyCombination edit_keyCombination = new KeyCodeCombination(KeyCode.E, KeyCombination.SHIFT_DOWN);

    /**
     * 暂停快捷键
     */
    public static final KeyCombination pause_keyCombination = new KeyCodeCombination(KeyCode.PAUSE);

    /**
     * 隐藏快捷键
     */
    public final static KeyCombination hide_keyCombination = new KeyCodeCombination(KeyCode.H, KeyCombination.SHIFT_DOWN);

    static {
        if (OSUtil.isMacOS()) {
            save_keyCombination = new KeyCodeCombination(KeyCode.S, KeyCombination.META_DOWN);
            selectAll_keyCombination = new KeyCodeCombination(KeyCode.A, KeyCombination.META_DOWN);
            undo_keyCombination = new KeyCodeCombination(KeyCode.Z, KeyCombination.META_DOWN);
            redo_keyCombination = new KeyCodeCombination(KeyCode.Y, KeyCombination.META_DOWN);
            cut_keyCombination = new KeyCodeCombination(KeyCode.X, KeyCombination.META_DOWN);
            paste_keyCombination = new KeyCodeCombination(KeyCode.V, KeyCombination.META_DOWN);
            copy_keyCombination = new KeyCodeCombination(KeyCode.C, KeyCombination.META_DOWN);
            search_keyCombination = new KeyCodeCombination(KeyCode.F, KeyCombination.META_DOWN);
            run_keyCombination = new KeyCodeCombination(KeyCode.R, KeyCombination.META_DOWN);
            stop_keyCombination = new KeyCodeCombination(KeyCode.S, KeyCombination.META_DOWN);
            apply_keyCombination = new KeyCodeCombination(KeyCode.T, KeyCombination.META_DOWN);
        } else {
            save_keyCombination = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);
            selectAll_keyCombination = new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN);
            undo_keyCombination = new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN);
            redo_keyCombination = new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN);
            cut_keyCombination = new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN);
            paste_keyCombination = new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN);
            copy_keyCombination = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN);
            search_keyCombination = new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN);
            run_keyCombination = new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN);
            stop_keyCombination = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);
            apply_keyCombination = new KeyCodeCombination(KeyCode.T, KeyCombination.META_DOWN);
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
        return save_keyCombination.match(event);
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
        return save_keyCombination.match(event);
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
        return cut_keyCombination.match(event);
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
        return copy_keyCombination.match(event);
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
        return selectAll_keyCombination.match(event);
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
        return undo_keyCombination.match(event);
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
        return redo_keyCombination.match(event);
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
        return paste_keyCombination.match(event);
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
        return run_keyCombination.match(event);
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

    /**
     * 获取按键的数字
     *
     * @param code 按键
     * @return 数字
     */
    public static int getDigit(KeyCode code) {
        if (code.isDigitKey()) {
            return switch (code) {
                case NUMPAD0, DIGIT0 -> 0;
                case NUMPAD1, DIGIT1 -> 1;
                case NUMPAD2, DIGIT2 -> 2;
                case NUMPAD3, DIGIT3 -> 3;
                case NUMPAD4, DIGIT4 -> 4;
                case NUMPAD5, DIGIT5 -> 5;
                case NUMPAD6, DIGIT6 -> 6;
                case NUMPAD7, DIGIT7 -> 7;
                case NUMPAD8, DIGIT8 -> 8;
                case NUMPAD9, DIGIT9 -> 9;
                default -> -1;
            };
        }
        return -1;
    }
}
