package cn.oyzh.fx.plus.adapter;

import cn.oyzh.fx.plus.handler.StateManager;
import cn.oyzh.fx.plus.popup.PopupWrapper;
import cn.oyzh.fx.plus.stage.StageWrapper;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.util.NodeUtil;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.NonNull;

/**
 * 状态适配器
 *
 * @author oyzh
 * @since 2023/3/15
 */
public interface StateAdapter extends PropAdapter {

    /**
     * 隐藏
     */
    default void disappear() {
        if (this instanceof Node node) {
            if (!node.visibleProperty().isBound()) {
                node.setVisible(false);
            }
            if (!node.managedProperty().isBound()) {
                node.setManaged(false);
            }
        } else if (this instanceof MenuItem item) {
            if (!item.visibleProperty().isBound()) {
                item.setVisible(false);
            }
        } else if (this instanceof Tab tab) {
            if (tab.getContent() != null && !tab.getContent().visibleProperty().isBound()) {
                tab.getContent().setVisible(false);
            }
        } else if (this instanceof Stage stage) {
            if (stage.isShowing()) {
                FXUtil.runWait(stage::close);
            }
        } else if (this instanceof Window window) {
            if (window.isShowing()) {
                FXUtil.runWait(window::hide);
            }
        } else if (this instanceof PopupWrapper wrapper) {
            if (wrapper.popup().isShowing()) {
                FXUtil.runWait(wrapper.popup()::hide);
            }
        } else if (this instanceof StageWrapper wrapper) {
            if (wrapper.stage().isShowing()) {
                FXUtil.runWait(wrapper.stage()::close);
            }
        }
    }

    /**
     * 显示
     */
    default void display() {
        if (this instanceof Node node) {
            if (!node.visibleProperty().isBound()) {
                node.setVisible(true);
            }
            if (!node.managedProperty().isBound()) {
                node.setManaged(true);
            }
        } else if (this instanceof MenuItem item) {
            if (!item.visibleProperty().isBound()) {
                item.setVisible(true);
            }
        } else if (this instanceof Tab tab) {
            if (tab.getContent() != null && !tab.getContent().visibleProperty().isBound()) {
                tab.getContent().setVisible(true);
            }
        } else if (this instanceof Stage stage) {
            if (!stage.isShowing()) {
                FXUtil.runWait(stage::show);
            }
        } else if (this instanceof StageWrapper stage) {
            if (!stage.stage().isShowing()) {
                FXUtil.runWait(stage.stage()::show);
            }
        }
    }

    /**
     * 禁用
     */
    default void disable() {
        NodeUtil.disable(this);
    }

    /**
     * 启用
     */
    default void enable() {
        NodeUtil.enable(this);
    }

    /**
     * managed属性绑定visible属性
     */
    default void managedBindVisible() {
        if (this instanceof Node node) {
            this.managedBindVisible(node);
        } else if (this instanceof Stage stage) {
            Scene scene = stage.getScene();
            if (scene != null && scene.getRoot() != null && !scene.getRoot().disableProperty().isBound()) {
                this.managedBindVisible(scene.getRoot());
            }
        } else if (this instanceof StageWrapper stage) {
            if (stage.root() != null && !stage.root().disableProperty().isBound()) {
                this.managedBindVisible(stage.root());
            }
        }
    }

    /**
     * managed属性绑定visible属性
     *
     * @param other 其他节点
     */
    default void managedBindVisible(@NonNull Node other) {
        if (this instanceof Node node) {
            if (!node.managedProperty().isBound()) {
                node.managedProperty().bind(other.visibleProperty());
            }
        } else if (this instanceof Tab tab) {
            if (tab.getContent() != null && !tab.getContent().visibleProperty().isBound()) {
                tab.getContent().managedProperty().bind(other.visibleProperty());
            }
        }
    }

    /**
     * 设置状态管理器
     *
     * @param manager 状态管理器
     */
    void setStateManager(StateManager manager);

    /**
     * 获取状态管理器
     *
     * @return 状态管理器
     */
    StateManager getStateManager();

    /**
     * 设置状态管理器
     *
     * @param manager 状态管理器
     */
    default void stateManager(StateManager manager) {
        if (manager == null) {
            return;
        }
        Node node;
        if (this instanceof Node node1) {
            node = node1;
        } else if (this instanceof Tab tab) {
            node = tab.getContent();
        } else {
            node = null;
        }
        if (node != null) {
            if (!node.visibleProperty().isBound()) {
                node.visibleProperty().bind(manager.visibleProperty());
//                manager.visibleProperty().addListener((observableValue, aBoolean, t1) -> node.setVisible(t1));
//                manager.visibleProperty().addListener(new WeakChangeListener<>((observableValue, aBoolean, t1) -> node.setVisible(t1)));
            }
            if (!node.managedProperty().isBound()) {
                node.managedProperty().bind(manager.managedProperty());
//                manager.managedProperty().addListener((observableValue, aBoolean, t1) -> node.setManaged(t1));
//                manager.managedProperty().addListener(new WeakChangeListener<>((observableValue, aBoolean, t1) -> node.setManaged(t1)));
            }
            if (!node.disableProperty().isBound()) {
                node.disableProperty().bind(manager.disableProperty());
//                manager.disableProperty().addListener((observableValue, aBoolean, t1) -> node.setDisable(t1));
//                manager.disableProperty().addListener(new WeakChangeListener<>((observableValue, aBoolean, t1) -> node.setDisable(t1)));
            }
        }
        this.setProp("_stateManager", manager);
    }

    /**
     * 获取状态管理器
     *
     * @return 状态管理器
     */
    default StateManager stateManager() {
        return this.getProp("_stateManager");
    }
}
