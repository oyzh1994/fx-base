package cn.oyzh.fx.plus.keyboard;

import javafx.scene.input.KeyCode;
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
     * 是否按下ctrl s
     *
     * @param event 事件
     * @return 结果
     */
    public static boolean isCtrlS(KeyEvent event) {
        if (event == null) {
            return false;
        }
        return event.isControlDown() && event.getCode() == KeyCode.S;
    }
}
