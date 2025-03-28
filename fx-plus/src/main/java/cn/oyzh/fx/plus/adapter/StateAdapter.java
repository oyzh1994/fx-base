package cn.oyzh.fx.plus.adapter;

import cn.oyzh.fx.plus.node.NodeUtil;
import cn.oyzh.fx.plus.window.StageAdapter;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.stage.Stage;

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
        NodeUtil.disappear(this);
    }

    /**
     * 显示
     */
    default void display() {
        NodeUtil.display(this);
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
     * 设置managed属性绑定visible属性
     *
     * @param managedBindVisible 是否managed属性绑定visible属性
     */
    default void setManagedBindVisible(boolean managedBindVisible) {
        if (managedBindVisible) {
            this.managedBindVisible();
        }
        this.setProp("_managed_bind_visible", managedBindVisible);
    }

    /**
     * 是否managed属性绑定visible属性
     */
    default boolean isManagedBindVisible() {
        return this.getProp("_managed_bind_visible");
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
        } else if (this instanceof StageAdapter stage) {
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
    default void managedBindVisible( Node other) {
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
}
