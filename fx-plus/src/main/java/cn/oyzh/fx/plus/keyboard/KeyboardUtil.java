package cn.oyzh.fx.plus.keyboard;

import cn.oyzh.common.util.OSUtil;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import lombok.experimental.UtilityClass;

/**
 * 键盘按键事件
 *
 * @author oyzh
 * @since 2024/07/02
 */
@UtilityClass
public class KeyboardUtil {

    /**
     * 保存键组合
     */
    public static KeyCombination saveKeyCombination;

    static {
        if (OSUtil.isMacOS()) {
            saveKeyCombination = new KeyCodeCombination(KeyCode.S, KeyCombination.META_DOWN);
        } else {
            saveKeyCombination = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);
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
        return event.isControlDown() && event.getCode() == KeyCode.R;
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
