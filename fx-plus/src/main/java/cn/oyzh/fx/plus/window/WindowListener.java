package cn.oyzh.fx.plus.window;

import javafx.stage.WindowEvent;

/**
 * 窗口监听接口
 *
 * @author oyzh
 * @since 2024/07/12
 */
public interface WindowListener {

    /**
     * 窗口显示中事件
     *
     * @param event 事件
     */
    void onWindowShowing(WindowEvent event);

    /**
     * 窗口隐藏中事件
     *
     * @param event 事件
     */
    void onWindowHiding(WindowEvent event);

    /**
     * 窗口已隐藏事件
     *
     * @param event 事件
     */
    void onWindowHidden(WindowEvent event);

    /**
     * 窗口已显示事件
     *
     * @param event 事件
     */
    void onWindowShown(WindowEvent event);

    /**
     * 窗口请求关闭事件
     *
     * @param event 事件
     */
    void onWindowCloseRequest(WindowEvent event);
}
