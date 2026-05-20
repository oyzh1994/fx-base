package cn.oyzh.fx.plus.window;

import cn.oyzh.common.util.ArrayUtil;
import cn.oyzh.fx.plus.ext.FXMLLoaderExt;
import cn.oyzh.fx.plus.handler.EscHideHandler;
import cn.oyzh.fx.plus.handler.TabSwitchHandler;
import cn.oyzh.fx.plus.node.NodeDestroyUtil;
import cn.oyzh.fx.plus.util.CursorUtil;
import cn.oyzh.fx.plus.util.StyleUtil;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.PopupWindow;
import javafx.stage.Window;

import java.util.function.Consumer;

/**
 * 弹窗适配器
 *
 * @author oyzh
 * @since 2024/07/12
 */
public interface PopupAdapter extends WindowAdapter {

    @Override
    default void onWindowClosed() {
        try {
            WindowAdapter.super.onWindowClosed();
            this.content(null);
//            NodeDestroyUtil.destroyObject(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 获取弹窗
     *
     * @return 弹窗
     */
    PopupWindow popup();

    /**
     * 初始化监听器
     *
     * @param listener 弹窗监听器
     */
    default void initListener(PopupListener listener) {
        // 设置事件
        listener.onPopupInitialize(this);
        this.popup().setOnShown(listener::onWindowShown);
        this.popup().setOnHiding(listener::onWindowHiding);
        this.popup().setOnHidden(listener::onWindowHidden);
        this.popup().setOnShowing(listener::onWindowShowing);
        this.popup().setOnCloseRequest(listener::onWindowCloseRequest);
    }


    @Override
    default void handCursor() {
        CursorUtil.handCursor(this.popup());
    }

    @Override
    default void waitCursor() {
        CursorUtil.waitCursor(this.popup());
    }

    @Override
    default void defaultCursor() {
        CursorUtil.defaultCursor(this.popup());
    }

    @Override
    default void hideOnEscape() {
        this.setProp("escHideHandler", new EscHideHandler(this.popup()));
    }

    @Override
    default void switchOnTab() {
        this.setProp("tabSwitchHandler", new TabSwitchHandler(this.popup()));
    }

    /**
     * 显示弹窗
     *
     * @param owner 父组件
     */
    default void showPopup(Node owner) {
        Point2D pos = owner.localToScreen(0, 0);
        if (pos == null) {
            this.popup().show(owner, 0, 0);
        } else {
            this.popup().show(owner, pos.getX(), pos.getY());
        }
    }

    /**
     * 显示弹窗
     *
     * @param owner 父组件
     * @param x     x位置
     * @param y     y位置
     */
    default void showPopup(Node owner, double x, double y) {
        this.popup().show(owner, x, y);
    }

    /**
     * 显示弹窗
     *
     * @param owner 父组件
     */
    default void showPopup(Window owner) {
        this.popup().show(owner);
    }

    /**
     * 显示弹窗
     *
     * @param owner 父组件
     * @param x     x位置
     * @param y     y位置
     */
    default void showPopup(Window owner, double x, double y) {
        this.popup().show(owner, x, y);
    }

    /**
     * 获取内容
     *
     * @return 内容
     */
    Node content();

    /**
     * 设置内容
     *
     * @param content 内容组件
     */
    void content(Node content);

    /**
     * 获取控制器
     *
     * @return 控制器
     */
    default Object getController() {
        return this.getProp("_controller");
    }

    /**
     * 初始化弹窗
     *
     * @param attribute 弹窗属性
     */
    default void init(PopupAttribute attribute) {
        // 初始化加载器
        FXMLLoaderExt loader = new FXMLLoaderExt();
        // 加载根节点
        Parent root = loader.load(attribute.value());
        if (root == null) {
            throw new RuntimeException("load root fail");
        }
        // 设置内容
        this.content(root);
        // 设置controller
        this.setProp("_controller", loader.getController());
        // 加载自定义css文件
        if (ArrayUtil.isNotEmpty(attribute.cssUrls())) {
            root.getStylesheets().setAll(StyleUtil.split(attribute.cssUrls()));
        }
        // 设置事件
        if (this.controller() instanceof PopupListener listener) {
            this.initListener(listener);
        }
    }

    /**
     * 设置弹窗提交处理器
     *
     * @param handler 弹窗提交处理器
     * @param <T>     泛型
     */
    default <T> void setSubmitHandler(Consumer<T> handler) {
        setProp("_submitHandler", handler);
    }

    /**
     * 获取弹窗提交处理器
     *
     * @param <T> 泛型
     * @return 弹窗提交处理器
     */
    default <T> Consumer<T> getSubmitHandler() {
        return getProp("_submitHandler");
    }

    /**
     * 提交弹窗
     *
     * @param obj 对象
     * @param <T> 泛型
     */
    default <T> void submit(T obj) {
        Consumer<T> consumer = this.getSubmitHandler();
        if (consumer != null) {
            consumer.accept(obj);
        }
    }

    /**
     * 获取节点
     *
     * @return 父节点
     */
    default Node getOwnerNode() {
        return this.popup().getOwnerNode();
    }

    /**
     * 获取父窗口
     *
     * @return 父窗口
     */
    default Window getOwnerWindow() {
        return this.popup().getOwnerWindow();
    }

    /**
     * 是否显示中
     *
     * @return 结果
     */
    default boolean isShowing() {
        return this.popup().isShowing();
    }

    default void show(Node node) {
        this.popup().show(node, 0, 0);
    }

    default void hide() {
        this.popup().hide();
    }

    @Override
    default Scene scene() {
        if (this.popup() != null) {
            return this.popup().getScene();
        }
        return null;
    }
}
