package cn.oyzh.fx.plus.adapter;

import cn.oyzh.fx.plus.view.FXStage;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TableColumnBase;
import javafx.stage.Window;

/**
 * 属性适配器
 *
 * @author oyzh
 * @since 2023/4/11
 */
public interface PropAdapter {

    /**
     * 设置属性
     *
     * @param key 键
     * @param val 值
     */
    default void setProp(String key, Object val) {
        if (this instanceof Node node) {
            node.getProperties().put(key, val);
        } else if (this instanceof TableColumnBase<?, ?> columnBase) {
            columnBase.getProperties().put(key, val);
        } else if (this instanceof Scene scene) {
            scene.getProperties().put(key, val);
        } else if (this instanceof Window window) {
            window.getProperties().put(key, val);
        } else if (this instanceof FXStage stage && stage.getStage() != null) {
            stage.getStage().getProperties().put(key, val);
        }
    }

    /**
     * 获取属性
     *
     * @param key 键
     * @return 值
     */
    default <T> T getProp(String key) {
        if (this instanceof Node node) {
            return (T) node.getProperties().get(key);
        } else if (this instanceof TableColumnBase<?, ?> columnBase) {
            return (T) columnBase.getProperties().get(key);
        } else if (this instanceof Scene scene) {
            return (T) scene.getProperties().get(key);
        } else if (this instanceof Window window) {
            return (T) window.getProperties().get(key);
        } else if (this instanceof FXStage stage && stage.getStage() != null) {
            return (T) stage.getStage().getProperties().get(key);
        }
        return null;
    }

    /**
     * 是否存在属性
     *
     * @param key 键
     * @return 结果
     */
    default boolean hasProp(String key) {
        if (this instanceof Node node) {
            return node.getProperties().containsKey(key);
        } else if (this instanceof TableColumnBase<?, ?> columnBase) {
            return columnBase.getProperties().containsKey(key);
        } else if (this instanceof Scene scene) {
            return scene.getProperties().containsKey(key);
        } else if (this instanceof Window window) {
            return window.getProperties().containsKey(key);
        } else if (this instanceof FXStage stage && stage.getStage() != null) {
            return stage.getStage().getProperties().containsKey(key);
        }
        return false;
    }

    /**
     * 移除值
     *
     * @param key 键
     * @param <T> 值类型
     * @return 值
     */
    default <T> T removeProp(String key) {
        if (this instanceof Node node) {
            return (T) node.getProperties().remove(key);
        } else if (this instanceof TableColumnBase<?, ?> columnBase) {
            return (T) columnBase.getProperties().remove(key);
        } else if (this instanceof Scene scene) {
            return (T) scene.getProperties().remove(key);
        } else if (this instanceof Window window) {
            return (T) window.getProperties().remove(key);
        } else if (this instanceof FXStage stage && stage.getStage() != null) {
            return (T) stage.getStage().getProperties().remove(key);
        }
        return null;
    }

    /**
     * 清除属性
     */
    default void clearProps() {
        if (this instanceof Node node) {
            node.getProperties().clear();
        } else if (this instanceof TableColumnBase<?, ?> columnBase) {
            columnBase.getProperties().clear();
        } else if (this instanceof Scene scene) {
            scene.getProperties().clear();
        } else if (this instanceof Window window) {
            window.getProperties().clear();
        } else if (this instanceof FXStage stage && stage.getStage() != null) {
            stage.getStage().getProperties().clear();
        }
    }
}
