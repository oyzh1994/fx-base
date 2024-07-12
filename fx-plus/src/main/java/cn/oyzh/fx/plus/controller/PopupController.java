package cn.oyzh.fx.plus.controller;

import cn.oyzh.fx.plus.i18n.I18nAdapter;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.window.PopupListener;
import cn.oyzh.fx.plus.window.PopupWrapper;
import javafx.stage.WindowEvent;
import lombok.Getter;
import lombok.NonNull;

import java.util.Locale;

/**
 * 组件控制器
 *
 * @author oyzh
 * @since 2024/07/12
 */
public class PopupController extends BaseController implements PopupListener {

    /**
     * 窗口
     */
    @Getter
    protected PopupWrapper window;

    /**
     * 设置窗口
     *
     * @param window 窗口
     */
    protected void setWindow(@NonNull PopupWrapper window) {
        this.window = window;
    }

    @Override
    public void onPopupInitialize(PopupWrapper window) {
        // 设置页面
        this.setWindow(window);
        NodeManager.init(this);
    }

    @Override
    public void onWindowShowing(WindowEvent event) {
        this.bindListeners();
    }

    @Override
    public void onWindowHiding(WindowEvent event) {
    }

    @Override
    public void onWindowHidden(WindowEvent event) {
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

    @Override
    public void changeLocale(Locale locale) {

    }
}
