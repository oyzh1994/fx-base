//package cn.oyzh.fx.plus.view;
//
//import cn.oyzh.fx.plus.adapter.PropAdapter;
//import cn.oyzh.fx.plus.handler.EscHideHandler;
//import cn.oyzh.fx.plus.handler.TabSwitchHandler;
//import cn.oyzh.fx.plus.util.CursorUtil;
//import cn.oyzh.fx.plus.util.FXUtil;
//import cn.oyzh.fx.plus.util.NodeUtil;
//import javafx.event.EventHandler;
//import javafx.scene.Parent;
//import javafx.scene.Scene;
//import javafx.stage.Stage;
//import javafx.stage.Window;
//import javafx.stage.WindowEvent;
//import lombok.Getter;
//import lombok.NonNull;
//import lombok.extern.slf4j.Slf4j;
//
//import java.lang.ref.WeakReference;
//
///**
// * 窗口
// *
// * @author oyzh
// * @since 2023/3/2
// */
//@Deprecated
//@Slf4j
//public class FXStage implements PropAdapter {
//
//    /**
//     * 是否已使用过
//     */
//    @Getter
//    private boolean used;
//
//    /**
//     * 窗口引用
//     */
//    protected WeakReference<Stage> stageReference;
//
//    public FXStage() {
//    }
//
//    public FXStage(@NonNull Stage stage) {
//        this.initStage(stage);
//    }
//
//    /**
//     * 初始化stage
//     *
//     * @param stage stage
//     */
//    protected void initStage(Stage stage) {
//        // 创建引用
//        this.stageReference = new WeakReference<>(stage);
//        // 设置引用对象
//        stage.getProperties().put("_reference", this);
//        // 监听显示属性
//        stage.showingProperty().addListener((observable, oldValue, newValue) -> {
//            if (!newValue) {
//                this.onClosed();
//            } else if (!this.used) {
//                this.used = true;
//            }
//        });
//    }
//
//    /**
//     * 关闭事件
//     */
//    protected void onClosed() {
//        this.clear();
//    }
//
//    /**
//     * 执行清理
//     */
//    protected void clear() {
//        try {
//            this.clearProps();
//            if (this.stageReference != null) {
//                this.stageReference.clear();
//            }
//            this.stageReference = null;
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }
//
//    /**
//     * 显示
//     */
//    public void show() {
//        if (!this.isShowing()) {
//            FXUtil.runWait(this.getStage()::show);
//        }
//    }
//
//    /**
//     * 关闭
//     */
//    public void close() {
//        if (this.getStage() != null) {
//            FXUtil.runLater(this.getStage()::close);
//        }
//    }
//
//    /**
//     * 是否显示中
//     *
//     * @return 结果
//     */
//    public boolean isShowing() {
//        return this.getStage() != null && this.getStage().isShowing();
//    }
//
//    /**
//     * 获取窗口
//     *
//     * @return 窗口
//     */
//    public Stage getStage() {
//        return this.stageReference == null ? null : this.stageReference.get();
//    }
//
//    /**
//     * 获取舞台
//     *
//     * @return 舞台
//     */
//    public Scene getScene() {
//        Stage stage = this.getStage();
//        return stage == null ? null : stage.getScene();
//    }
//
//    /**
//     * 获取根节点
//     *
//     * @return 根节点
//     */
//    public Parent root() {
//        Stage stage = this.getStage();
//        if (stage == null || stage.getScene() == null) {
//            return null;
//        }
//        return stage.getScene().getRoot();
//    }
//
//    /**
//     * 禁用
//     */
//    public void disable() {
//        FXUtil.runLater(() -> this.root().setDisable(true));
//    }
//
//    /**
//     * 启用
//     */
//    public void enable() {
//        FXUtil.runLater(() -> this.root().setDisable(false));
//    }
//
//    /**
//     * hand鼠标样式
//     */
//    public void handCursor() {
//        CursorUtil.handCursor(this.getStage());
//    }
//
//    /**
//     * wait鼠标样式
//     */
//    public void waitCursor() {
//        CursorUtil.waitCursor(this.getStage());
//    }
//
//    /**
//     * 默认鼠标样式
//     */
//    public void defaultCursor() {
//        CursorUtil.defaultCursor(this.getStage());
//    }
//
//    /**
//     * 隐藏中事件处理
//     *
//     * @param handler 处理器
//     */
//    public void setOnHiding(EventHandler<WindowEvent> handler) {
//        if (this.getStage() != null) {
//            this.getStage().setOnHiding(handler);
//        }
//    }
//
//    /**
//     * 已隐藏事件处理
//     *
//     * @param handler 处理器
//     */
//    public void setOnHidden(EventHandler<WindowEvent> handler) {
//        if (this.getStage() != null) {
//            this.getStage().setOnHidden(handler);
//        }
//    }
//
//    /**
//     * 显示中事件处理
//     *
//     * @param handler 处理器
//     */
//    public void setOnShowing(EventHandler<WindowEvent> handler) {
//        if (this.getStage() != null) {
//            this.getStage().setOnShowing(handler);
//        }
//    }
//
//    /**
//     * 已显示事件处理
//     *
//     * @param handler 处理器
//     */
//    public void setOnShown(EventHandler<WindowEvent> handler) {
//        if (this.getStage() != null) {
//            this.getStage().setOnShown(handler);
//        }
//    }
//
//    /**
//     * 关闭请求事件处理
//     *
//     * @param handler 处理器
//     */
//    public void setOnCloseRequest(EventHandler<WindowEvent> handler) {
//        if (this.getStage() != null) {
//            this.getStage().setOnCloseRequest(handler);
//        }
//    }
//
//    /**
//     * 获取父窗口
//     *
//     * @return 父窗口
//     */
//    public Window getOwner() {
//        return this.getStage() != null ? this.getStage().getOwner() : null;
//    }
//
//    /**
//     * 获取宽度
//     *
//     * @return 宽度
//     */
//    public double getWidth() {
//        return NodeUtil.getWidth(this.getStage());
//    }
//
//    /**
//     * 设置宽度
//     *
//     * @param width 宽度
//     */
//    public void setWidth(double width) {
//        NodeUtil.setWidth(this.getStage(), width);
//    }
//
//    /**
//     * 获取高度
//     *
//     * @return 高度
//     */
//    public double getHeight() {
//        return NodeUtil.getHeight(this.getStage());
//    }
//
//    /**
//     * 设置高度
//     *
//     * @param height 高度
//     */
//    public void setHeight(double height) {
//        NodeUtil.setHeight(this.getStage(), height);
//    }
//
//    /**
//     * 获取页面x
//     *
//     * @return 页面x
//     */
//    public double getX() {
//        if (this.getStage() != null) {
//            return this.getStage().getX();
//        }
//        return Double.NaN;
//    }
//
//    /**
//     * 设置页面x
//     *
//     * @param x 页面x
//     */
//    public void setX(double x) {
//        if (this.getStage() != null) {
//            this.getStage().setX(x);
//        }
//    }
//
//    /**
//     * 获取页面y
//     *
//     * @return 页面y
//     */
//    public double getY() {
//        if (this.getStage() != null) {
//            return this.getStage().getY();
//        }
//        return Double.NaN;
//    }
//
//    /**
//     * 设置页面y
//     *
//     * @param y 页面y
//     */
//    public void setY(double y) {
//        if (this.getStage() != null) {
//            this.getStage().setY(y);
//        }
//    }
//
//    /**
//     * 页面是否最大化
//     *
//     * @return 结果
//     */
//    public boolean isMaximized() {
//        if (this.getStage() != null) {
//            return this.getStage().isMaximized();
//        }
//        return false;
//    }
//
//    /**
//     * 设置页面最大化
//     *
//     * @param maximized 是否最大化
//     */
//    public void setMaximized(boolean maximized) {
//        if (this.getStage() != null) {
//            this.getStage().setMaximized(maximized);
//        }
//    }
//
//    /**
//     * 前端显示
//     */
//    public void toFront() {
//        Stage stage = this.getStage();
//        if (stage != null) {
//            FXUtil.runLater(() -> {
//                stage.toFront();
//                stage.setIconified(false);
//            });
//        }
//    }
//
//    /**
//     * 设置按下eac时隐藏窗口
//     */
//    public void hideOnEscape() {
//        if (this.getStage() != null && !EscHideHandler.exists(this.getStage())) {
//            EscHideHandler.init(this.getStage());
//        }
//    }
//
//    /**
//     * 取消按下eac时隐藏窗口
//     */
//    public void unHideOnEscape() {
//        EscHideHandler.destroy(this.getStage());
//    }
//
//    /**
//     * 是否按下esc时隐藏窗口
//     *
//     * @return 结果
//     */
//    public boolean isHideOnEscape() {
//        return EscHideHandler.exists(this.getStage());
//    }
//
//    /**
//     * 设置按下tab时切换组件
//     */
//    public void switchOnTab() {
//        if (this.getStage() != null && !TabSwitchHandler.exists(this.getStage())) {
//            TabSwitchHandler.init(this.getStage());
//        }
//    }
//
//    /**
//     * 取消按下tab时切换组件
//     */
//    public void unSwitchOnTab() {
//        TabSwitchHandler.destroy(this.getStage());
//    }
//
//    /**
//     * 是否按下tab时切换组件
//     *
//     * @return 结果
//     */
//    public boolean isSwitchOnTab() {
//        return TabSwitchHandler.exists(this.getStage());
//    }
//}
