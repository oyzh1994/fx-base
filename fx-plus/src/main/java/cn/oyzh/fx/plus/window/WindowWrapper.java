package cn.oyzh.fx.plus.window;

import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.handler.StateManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;

/**
 * 窗口包装
 *
 * @author oyzh
 * @since 2024/07/12
 */
public interface WindowWrapper extends StateAdapter, ThemeAdapter {

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
    void handCursor();

    /**
     * wait鼠标样式
     */
    void waitCursor();

    /**
     * 默认鼠标样式
     */
    void defaultCursor();

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
    void hideOnEscape();

    /**
     * 取消按下eac时隐藏窗口
     */
    void unHideOnEscape();

    /**
     * 是否按下esc时隐藏窗口
     *
     * @return 结果
     */
    boolean isHideOnEscape();

    /**
     * 设置按下tab时切换组件
     */
    void switchOnTab();

    /**
     * 取消按下tab时切换组件
     */
    void unSwitchOnTab();

    /**
     * 是否按下tab时切换组件
     *
     * @return 结果
     */
    boolean isSwitchOnTab();

    @Override
    default void setStateManager(StateManager manager) {
        StateAdapter.super.stateManager(manager);
    }

    @Override
    default StateManager getStateManager() {
        return StateAdapter.super.stateManager();
    }
}
