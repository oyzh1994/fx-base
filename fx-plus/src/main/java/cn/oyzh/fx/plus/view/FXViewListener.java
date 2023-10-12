package cn.oyzh.fx.plus.view;

import javafx.stage.WindowEvent;

/**
 * 页面监听接口
 *
 * @author oyzh
 * @since 2023/3/13
 */
@Deprecated
public interface FXViewListener {

    /**
     * 页面初始化事件
     *
     * @param view 页面
     */
    void onViewInitialize(FXView view);

    /**
     * 页面显示中事件
     *
     * @param event 事件
     */
    void onViewShowing(WindowEvent event);

    /**
     * 页面已显示事件
     *
     * @param event 事件
     */
    void onViewShown(WindowEvent event);

    /**
     * 页面请求关闭事件
     *
     * @param event 事件
     */
    void onViewCloseRequest(WindowEvent event);

    /**
     * 页面隐藏中事件
     *
     * @param event 事件
     */
    void onViewHiding(WindowEvent event);

    /**
     * 页面已隐藏事件
     *
     * @param event 事件
     */
    void onViewHidden(WindowEvent event);

    /**
     * 系统退出事件
     */
    void onSystemExit();
}
