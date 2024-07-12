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
public interface PopupWrapper extends WindowWrapper {

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
    default void initListener(@NonNull PopupListener listener) {
        // 设置事件
        listener.onPopupInitialize(this);
        this.popup().setOnHiding(listener::onWindowHiding);
        this.popup().setOnHidden(listener::onWindowHidden);
        this.popup().setOnShowing(listener::onWindowShowing);
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
        if (!EscHideHandler.exists(this.popup())) {
            EscHideHandler.init(this.popup());
        }
    }

    @Override
    default void unHideOnEscape() {
        EscHideHandler.destroy(this.popup());
    }

    @Override
    default boolean isHideOnEscape() {
        return EscHideHandler.exists(this.popup());
    }

    @Override
    default void switchOnTab() {
        if (!TabSwitchHandler.exists(this.popup())) {
            TabSwitchHandler.init(this.popup());
        }
    }

    @Override
    default void unSwitchOnTab() {
        TabSwitchHandler.destroy(this.popup());
    }

    @Override
    default boolean isSwitchOnTab() {
        return TabSwitchHandler.exists(this.popup());
    }

    /**
     * 显示弹窗
     *
     * @param owner 父组件
     */
    void showPopup(Node owner);

    /**
     * 设置内容
     *
     * @param content 内容组件
     */
    void setContent(Node content);

    /**
     * 初始化弹窗
     *
     * @param attribute 弹窗属性
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
        // 设置弹窗样式
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
