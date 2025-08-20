package cn.oyzh.fx.plus.util;

import javafx.event.EventTarget;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumnBase;
import javafx.stage.Window;

/**
 * @author oyzh
 * @since 2025-08-20
 */
public class PropertiesUtil {

    /**
     * 属性是否存在
     *
     * @param node 节点
     * @param key  键
     * @return 结果
     */
    public static boolean has(EventTarget node, Object key) {
        if (node == null || key == null) {
            return false;
        }
        if (node instanceof Node node1 && node1.hasProperties()) {
            return node1.getProperties().containsKey(key);
        }
        if (node instanceof Tab node1 && node1.hasProperties()) {
            return node1.getProperties().containsKey(key);
        }
        if (node instanceof Scene node1 && node1.hasProperties()) {
            return node1.getProperties().containsKey(key);
        }
        if (node instanceof Window node1 && node1.hasProperties()) {
            return node1.getProperties().containsKey(key);
        }
        if (node instanceof TableColumnBase<?, ?> node1 && node1.hasProperties()) {
            return node1.getProperties().containsKey(key);
        }
        return false;
    }

    /**
     * 移除属性
     *
     * @param node 节点
     * @param key  键
     * @return 属性
     */
    public static Object remove(EventTarget node, Object key) {
        if (node == null || key == null) {
            return null;
        }
        if (node instanceof Node node1 && node1.hasProperties()) {
            return node1.getProperties().remove(key);
        }
        if (node instanceof Tab node1 && node1.hasProperties()) {
            return node1.getProperties().remove(key);
        }
        if (node instanceof Scene node1 && node1.hasProperties()) {
            return node1.getProperties().remove(key);
        }
        if (node instanceof Window node1 && node1.hasProperties()) {
            return node1.getProperties().remove(key);
        }
        if (node instanceof TableColumnBase<?, ?> node1 && node1.hasProperties()) {
            return node1.getProperties().remove(key);
        }
        return null;
    }

    /**
     * 移除属性
     *
     * @param node  节点
     * @param key   键
     * @param value 值
     * @return 属性
     */
    public static Object remove(EventTarget node, Object key, Object value) {
        if (node == null || key == null) {
            return null;
        }
        if (node instanceof Node node1 && node1.hasProperties()) {
            return node1.getProperties().remove(key, value);
        }
        if (node instanceof Tab node1 && node1.hasProperties()) {
            return node1.getProperties().remove(key, value);
        }
        if (node instanceof Scene node1 && node1.hasProperties()) {
            return node1.getProperties().remove(key, value);
        }
        if (node instanceof Window node1 && node1.hasProperties()) {
            return node1.getProperties().remove(key, value);
        }
        if (node instanceof TableColumnBase<?, ?> node1 && node1.hasProperties()) {
            return node1.getProperties().remove(key, value);
        }
        return null;
    }

    /**
     * 获取属性
     *
     * @param node 节点
     * @param key  键
     * @return 值
     */
    public static Object get(EventTarget node, Object key) {
        if (node == null || key == null) {
            return null;
        }
        if (node instanceof Node node1 && node1.hasProperties()) {
            return node1.getProperties().get(key);
        }
        if (node instanceof Tab node1 && node1.hasProperties()) {
            return node1.getProperties().get(key);
        }
        if (node instanceof Scene node1 && node1.hasProperties()) {
            return node1.getProperties().get(key);
        }
        if (node instanceof Window node1 && node1.hasProperties()) {
            return node1.getProperties().get(key);
        }
        if (node instanceof TableColumnBase<?, ?> node1 && node1.hasProperties()) {
            return node1.getProperties().get(key);
        }
        return null;
    }

    /**
     * 设置属性
     *
     * @param node  节点
     * @param key   键
     * @param value 值
     */
    public static void set(EventTarget node, Object key, Object value) {
        if (node == null || key == null) {
            return;
        }
        if (node instanceof Node node1) {
            node1.getProperties().put(key, value);
        } else if (node instanceof Tab node1) {
            node1.getProperties().put(key, value);
        } else if (node instanceof Scene node1) {
            node1.getProperties().put(key, value);
        } else if (node instanceof Window node1) {
            node1.getProperties().put(key, value);
        } else if (node instanceof TableColumnBase<?, ?> node1) {
            node1.getProperties().put(key, value);
        }
    }

    /**
     * 清除属性
     *
     * @param node 节点
     */
    public static void clear(EventTarget node) {
        if (node == null) {
            return;
        }
        if (node instanceof Node node1 && node1.hasProperties()) {
            node1.getProperties().clear();
        } else if (node instanceof Tab node1 && node1.hasProperties()) {
            node1.getProperties().clear();
        } else if (node instanceof Scene node1 && node1.hasProperties()) {
            node1.getProperties().clear();
        } else if (node instanceof Window node1 && node1.hasProperties()) {
            node1.getProperties().clear();
        } else if (node instanceof TableColumnBase<?, ?> node1 && node1.hasProperties()) {
            node1.getProperties().clear();
        }
    }

}
