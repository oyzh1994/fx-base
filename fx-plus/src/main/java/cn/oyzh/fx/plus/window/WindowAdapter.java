package cn.oyzh.fx.plus.window;

import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.handler.EscHideHandler;
import cn.oyzh.fx.plus.handler.TabSwitchHandler;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.scene.Parent;
import javafx.scene.Scene;

/**
 * 窗口适配器
 *
 * @author oyzh
 * @since 2024/07/12
 */
public interface WindowAdapter extends StateAdapter, ThemeAdapter {

    /**
     * 关闭事件
     */
    default void onWindowClosed() {
        try {
            this.unSwitchOnTab();
            this.unHideOnEscape();
//            // 延迟清理
//            TaskManager.startDelay(this::clearProps, 100);
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
    default void unHideOnEscape(){
        EscHideHandler escHideHandler = this.removeProp("escHideHandler");
        if (escHideHandler != null) {
            escHideHandler.destroy();
        }
    }

    /**
     * 是否按下esc时隐藏窗口
     *
     * @return 结果
     */
    default boolean isHideOnEscape(){
        return this.hasProp("escHideHandler");
    }

    /**
     * 设置按下tab时切换组件
     */
    void switchOnTab();

    /**
     * 取消按下tab时切换组件
     */
    default void unSwitchOnTab(){
        TabSwitchHandler tabSwitchHandler = this.removeProp("tabSwitchHandler");
        if (tabSwitchHandler != null) {
            tabSwitchHandler.destroy();
        }
    }

    /**
     * 是否按下tab时切换组件
     *
     * @return 结果
     */
    default boolean isSwitchOnTab(){
        return this.hasProp("tabSwitchHandler");
    }

    /**
     * 获取场景
     *
     * @return 场景
     */
     Scene scene() ;

    /**
     * 获取根节点
     *
     * @return 根节点
     */
    default Parent root() {
        if (this.scene() != null) {
            return this.scene().getRoot();
        }
        return null;
    }

}
