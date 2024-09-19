package cn.oyzh.fx.plus.adapter;

import cn.oyzh.fx.plus.window.PopupAdapter;
import cn.oyzh.fx.plus.window.StageAdapter;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
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
        } else if (this instanceof Tab tab) {
            tab.getProperties().put(key, val);
        } else if (this instanceof TableColumnBase<?, ?> columnBase) {
            columnBase.getProperties().put(key, val);
        } else if (this instanceof Scene scene) {
            scene.getProperties().put(key, val);
        } else if (this instanceof Window window) {
            window.getProperties().put(key, val);
        } else if (this instanceof PopupAdapter wrapper && wrapper.popup() != null) {
            wrapper.popup().getProperties().put(key, val);
        } else if (this instanceof StageAdapter stage && stage.stage() != null) {
            stage.stage().getProperties().put(key, val);
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
            if (node.hasProperties()) {
                return (T) node.getProperties().get(key);
            }
        } else if (this instanceof Tab tab) {
            if (tab.hasProperties()) {
                return (T) tab.getProperties().get(key);
            }
        } else if (this instanceof TableColumnBase<?, ?> columnBase) {
            if (columnBase.hasProperties()) {
                return (T) columnBase.getProperties().get(key);
            }
        } else if (this instanceof Scene scene) {
            if (scene.hasProperties()) {
                return (T) scene.getProperties().get(key);
            }
        } else if (this instanceof Window window) {
            if (window.hasProperties()) {
                return (T) window.getProperties().get(key);
            }
        } else if (this instanceof PopupAdapter wrapper) {
            if (wrapper.popup().hasProperties()) {
                return (T) wrapper.popup().getProperties().get(key);
            }
        } else if (this instanceof StageAdapter stage && stage.stage() != null) {
            if (stage.stage().hasProperties()) {
                return (T) stage.stage().getProperties().get(key);
            }
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
        }
        if (this instanceof Tab tab) {
            return tab.getProperties().containsKey(key);
        }
        if (this instanceof TableColumnBase<?, ?> columnBase) {
            return columnBase.getProperties().containsKey(key);
        }
        if (this instanceof Scene scene) {
            return scene.getProperties().containsKey(key);
        }
        if (this instanceof Window window) {
            return window.getProperties().containsKey(key);
        }
        if (this instanceof PopupAdapter wrapper) {
            return wrapper.popup().getProperties().containsKey(key);
        }
        if (this instanceof StageAdapter stage && stage.stage() != null) {
            return stage.stage().getProperties().containsKey(key);
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
        } else if (this instanceof Tab tab) {
            return (T) tab.getProperties().remove(key);
        } else if (this instanceof TableColumnBase<?, ?> columnBase) {
            return (T) columnBase.getProperties().remove(key);
        } else if (this instanceof Scene scene) {
            return (T) scene.getProperties().remove(key);
        } else if (this instanceof Window window) {
            return (T) window.getProperties().remove(key);
        } else if (this instanceof PopupAdapter wrapper && wrapper.popup() != null) {
            return (T) wrapper.popup().getProperties().remove(key);
        } else if (this instanceof StageAdapter stage && stage.stage() != null) {
            return (T) stage.stage().getProperties().remove(key);
        }
        return null;
    }

    /**
     * 清除属性
     */
    default void clearProps() {
        if (this instanceof Node node) {
            node.getProperties().clear();
        } else if (this instanceof Tab tab) {
            tab.getProperties().clear();
        } else if (this instanceof TableColumnBase<?, ?> columnBase) {
            columnBase.getProperties().clear();
        } else if (this instanceof Scene scene) {
            scene.getProperties().clear();
        } else if (this instanceof Window window) {
            window.getProperties().clear();
        } else if (this instanceof PopupAdapter wrapper && wrapper.popup() != null) {
            wrapper.popup().getProperties().clear();
        } else if (this instanceof StageAdapter stage && stage.stage() != null) {
            stage.stage().getProperties().clear();
        }
    }
}
