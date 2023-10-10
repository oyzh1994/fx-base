package cn.oyzh.fx.plus.stage;

import javafx.stage.WindowEvent;

/**
 * 页面监听接口
 *
 * @author oyzh
 * @since 2023/3/13
 */
public interface StageListener {

    /**
     * 页面初始化事件
     *
     * @param stageExt 舞台扩展
     */
    void onStageInitialize(StageExt stageExt);

    /**
     * 页面显示中事件
     *
     * @param event 事件
     */
    void onStageShowing(WindowEvent event);

    /**
     * 页面已显示事件
     *
     * @param event 事件
     */
    void onStageShown(WindowEvent event);

    /**
     * 页面请求关闭事件
     *
     * @param event 事件
     */
    void onStageCloseRequest(WindowEvent event);

    /**
     * 页面隐藏中事件
     *
     * @param event 事件
     */
    void onStageHiding(WindowEvent event);

    /**
     * 页面已隐藏事件
     *
     * @param event 事件
     */
    void onStageHidden(WindowEvent event);

    /**
     * 系统退出事件
     */
    void onSystemExit();
}
