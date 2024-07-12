package cn.oyzh.fx.plus.popup;

import cn.oyzh.fx.plus.stage.StageWrapper;
import javafx.scene.control.PopupControl;
import javafx.stage.PopupWindow;
import javafx.stage.WindowEvent;

/**
 * 舞台监听接口
 *
 * @author oyzh
 * @since 2023/10/12
 */
public interface PopupListener {

    /**
     * 舞台初始化事件
     *
     * @param popup 舞台扩展
     */
    void onPopupInitialize(PopupWrapper popup);

    /**
     * 舞台显示中事件
     *
     * @param event 事件
     */
    void onPopupShowing(WindowEvent event);

    /**
     * 舞台隐藏中事件
     *
     * @param event 事件
     */
    void onPopupHiding(WindowEvent event);

    /**
     * 舞台已隐藏事件
     *
     * @param event 事件
     */
    void onPopupHidden(WindowEvent event);
}
