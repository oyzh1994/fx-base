package cn.oyzh.fx.plus.window;

import javafx.stage.WindowEvent;

/**
 * 窗口监听接口
 *
 * @author oyzh
 * @since 2024/07/12
 */
public interface PopupListener extends WindowListener {

    /**
     * 窗口初始化事件
     *
     * @param window 窗口扩展
     */
    void onPopupInitialize(PopupWrapper window);
}
