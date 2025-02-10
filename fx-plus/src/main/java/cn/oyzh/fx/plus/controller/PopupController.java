package cn.oyzh.fx.plus.controller;

import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.window.PopupAdapter;
import cn.oyzh.fx.plus.window.PopupListener;
import javafx.stage.WindowEvent;
import lombok.Getter;
import lombok.NonNull;

/**
 * 弹窗组件控制器
 *
 * @author oyzh
 * @since 2024/07/12
 */
public class PopupController extends Controller implements PopupListener {

    /**
     * 窗口
     */
    @Getter
    protected PopupAdapter window;

    /**
     * 设置窗口
     *
     * @param window 窗口
     */
    protected void setWindow(@NonNull PopupAdapter window) {
        this.window = window;
    }

    @Override
    public void onPopupInitialize(PopupAdapter window) {
        // 设置页面
        this.setWindow(window);
        NodeManager.init(this);
    }

    @Override
    protected void closeWindow() {
        if (this.window != null) {
            this.window.disappear();
        }
    }

    @Override
    protected <T> T getWindowProp(String key) {
        return this.window == null ? null : this.window.getProp(key);
    }

    protected <T> void submit(T obj) {
        this.window.submit(obj);
    }

    @Override
    public void onWindowShown(WindowEvent event) {

    }

    @Override
    public void onWindowCloseRequest(WindowEvent event) {

    }
}
