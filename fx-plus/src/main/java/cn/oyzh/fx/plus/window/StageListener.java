package cn.oyzh.fx.plus.window;

import javafx.stage.WindowEvent;

/**
 * 舞台监听接口
 *
 * @author oyzh
 * @since 2023/10/12
 */
public interface StageListener {

    /**
     * 舞台初始化事件
     *
     * @param stage 舞台扩展
     */
    void onStageInitialize(StageWrapper stage);

    /**
     * 舞台显示中事件
     *
     * @param event 事件
     */
    void onStageShowing(WindowEvent event);

    /**
     * 舞台已显示事件
     *
     * @param event 事件
     */
    void onStageShown(WindowEvent event);

    /**
     * 舞台请求关闭事件
     *
     * @param event 事件
     */
    void onStageCloseRequest(WindowEvent event);

    /**
     * 舞台隐藏中事件
     *
     * @param event 事件
     */
    void onStageHiding(WindowEvent event);

    /**
     * 舞台已隐藏事件
     *
     * @param event 事件
     */
    void onStageHidden(WindowEvent event);

    /**
     * 系统退出事件
     */
    void onSystemExit();
}
