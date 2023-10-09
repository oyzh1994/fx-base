package cn.oyzh.fx.plus.adapter;

import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import lombok.NonNull;

/**
 * 状态适配器
 *
 * @author oyzh
 * @since 2023/3/15
 */
public interface StateAdapter {

    /**
     * 隐藏节点
     */
    default void hideNode() {
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
        }
    }

    /**
     * 显示节点
     */
    default void showNode() {
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
        }
    }

    /**
     * managed属性绑定visible属性
     */
    default void managedBindVisible() {
        if (this instanceof Node node) {
            this.managedBindVisible(node);
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
}
