package cn.oyzh.fx.plus.window;

import cn.hutool.core.util.ArrayUtil;
import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.ext.FXMLLoaderExt;
import cn.oyzh.fx.plus.handler.EscHideHandler;
import cn.oyzh.fx.plus.handler.StateManager;
import cn.oyzh.fx.plus.handler.TabSwitchHandler;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.util.CursorUtil;
import cn.oyzh.fx.plus.util.StyleUtil;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.stage.PopupWindow;
import lombok.NonNull;

/**
 * 弹窗包装
 *
 * @author oyzh
 * @since 2024/07/12
 */
public interface PopupWrapper extends StateAdapter, ThemeAdapter {

    /**
     * 获取窗口
     *
     * @return 窗口
     */
    PopupWindow popup();

    /**
     * 初始化监听器
     *
     * @param listener 窗口监听器
     */
    default void initListener(@NonNull PopupListener listener) {
        // 设置事件
        listener.onPopupInitialize(this);
        this.popup().setOnHiding(listener::onPopupHiding);
        this.popup().setOnHidden(listener::onPopupHidden);
        this.popup().setOnShowing(listener::onPopupShowing);
    }

    /**
     * 关闭事件
     */
    default void onClosed() {
        try {
            this.unSwitchOnTab();
            this.unHideOnEscape();
            this.clearProps();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * hand鼠标样式
     */
    default void handCursor() {
        CursorUtil.handCursor(this.popup());
    }

    /**
     * wait鼠标样式
     */
    default void waitCursor() {
        CursorUtil.waitCursor(this.popup());
    }

    /**
     * 默认鼠标样式
     */
    default void defaultCursor() {
        CursorUtil.defaultCursor(this.popup());
    }

    /**
     * 获取controller
     *
     * @return controller
     */
    default Object controller() {
        return this.getProp("_controller");
    }

    /**
     * 获取controller类
     *
     * @return controller类
     */
    default Class<?> controllerClass() {
        Object controller = this.controller();
        return controller == null ? null : controller.getClass();
    }

    /**
     * 设置按下esc时隐藏窗口
     */
    default void hideOnEscape() {
        if (!EscHideHandler.exists(this.popup())) {
            EscHideHandler.init(this.popup());
        }
    }

    /**
     * 取消按下eac时隐藏窗口
     */
    default void unHideOnEscape() {
        EscHideHandler.destroy(this.popup());
    }

    /**
     * 是否按下esc时隐藏窗口
     *
     * @return 结果
     */
    default boolean isHideOnEscape() {
        return EscHideHandler.exists(this.popup());
    }

    /**
     * 设置按下tab时切换组件
     */
    default void switchOnTab() {
        if (!TabSwitchHandler.exists(this.popup())) {
            TabSwitchHandler.init(this.popup());
        }
    }

    /**
     * 取消按下tab时切换组件
     */
    default void unSwitchOnTab() {
        TabSwitchHandler.destroy(this.popup());
    }

    /**
     * 是否按下tab时切换组件
     *
     * @return 结果
     */
    default boolean isSwitchOnTab() {
        return TabSwitchHandler.exists(this.popup());
    }

    @Override
    default void setStateManager(StateManager manager) {
        StateAdapter.super.stateManager(manager);
    }

    @Override
    default StateManager getStateManager() {
        return StateAdapter.super.stateManager();
    }

    void showPopup(Node owner);

    void setContent(Node content);

    /**
     * 初始化窗口
     *
     * @param attribute 窗口属性
     */
    default void init(@NonNull PopupAttribute attribute) {
        // 初始化加载器
        FXMLLoaderExt loader = new FXMLLoaderExt();
        // 加载根节点
        Parent root = loader.load(attribute.value());
        if (root == null) {
            throw new RuntimeException("load root fail");
        }
        this.setContent(root);
        // 设置controller
        this.setProp("_controller", loader.getController());
        // 设置窗口样式
        // 加载自定义css文件
        if (ArrayUtil.isNotEmpty(attribute.cssUrls())) {
            root.getStylesheets().addAll(StyleUtil.split(attribute.cssUrls()));
        }
        // 设置事件
        if (this.controller() instanceof PopupListener listener) {
            this.initListener(listener);
        }
        // 监听显示属性
        this.popup().showingProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                this.onClosed();
            }
        });
    }

  
}
