package cn.oyzh.fx.plus.adapter;

import cn.oyzh.fx.plus.handler.StateManager;
import cn.oyzh.fx.plus.stage.StageWrapper;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.stage.Stage;
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
        } else if (this instanceof StageWrapper stage) {
            if (stage.stage().isShowing()) {
                FXUtil.runWait(stage.stage()::close);
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
        if (this instanceof Node node) {
            if (!node.disableProperty().isBound()) {
                node.setDisable(true);
            }
        } else if (this instanceof MenuItem item) {
            if (!item.disableProperty().isBound()) {
                item.setDisable(true);
            }
        } else if (this instanceof Tab tab) {
            if (!tab.disableProperty().isBound()) {
                tab.setDisable(true);
            }
        } else if (this instanceof Stage stage) {
            Scene scene = stage.getScene();
            if (scene != null && scene.getRoot() != null && !scene.getRoot().disableProperty().isBound()) {
                scene.getRoot().setDisable(true);
            }
        } else if (this instanceof StageWrapper stage) {
            if (stage.root() != null && !stage.root().disableProperty().isBound()) {
                stage.root().setDisable(true);
            }
        }
    }

    /**
     * 启用
     */
    default void enable() {
        if (this instanceof Node node) {
            if (!node.disableProperty().isBound()) {
                node.setDisable(false);
            }
        } else if (this instanceof MenuItem item) {
            if (!item.disableProperty().isBound()) {
                item.setDisable(false);
            }
        } else if (this instanceof Tab tab) {
            if (!tab.disableProperty().isBound()) {
                tab.setDisable(false);
            }
        } else if (this instanceof Stage stage) {
            Scene scene = stage.getScene();
            if (scene != null && scene.getRoot() != null && !scene.getRoot().disableProperty().isBound()) {
                scene.getRoot().setDisable(false);
            }
        } else if (this instanceof StageWrapper stage) {
            if (stage.root() != null && !stage.root().disableProperty().isBound()) {
                stage.root().setDisable(false);
            }
        }
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
                manager.visibleProperty().addListener((observableValue, aBoolean, t1) -> node.setVisible(t1));
            }
            if (!node.managedProperty().isBound()) {
                manager.managedProperty().addListener((observableValue, aBoolean, t1) -> node.setManaged(t1));
            }
            if (!node.disableProperty().isBound()) {
                manager.disableProperty().addListener((observableValue, aBoolean, t1) -> node.setDisable(t1));
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
