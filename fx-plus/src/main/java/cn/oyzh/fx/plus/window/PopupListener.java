package cn.oyzh.fx.plus.window;

/**
 * 弹窗监听接口
 *
 * @author oyzh
 * @since 2024/07/12
 */
public interface PopupListener extends WindowListener {

    /**
     * 弹窗初始化事件
     *
     * @param window 弹窗适配器
     */
    void onPopupInitialize(PopupAdapter window);
}
