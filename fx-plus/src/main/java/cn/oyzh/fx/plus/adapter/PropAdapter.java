package cn.oyzh.fx.plus.adapter;

import cn.oyzh.fx.plus.util.PropertiesUtil;
import cn.oyzh.fx.plus.window.PopupAdapter;
import cn.oyzh.fx.plus.window.StageAdapter;
import javafx.event.EventTarget;

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
        if (this instanceof PopupAdapter adapter && adapter.popup() != null) {
            PropertiesUtil.set(adapter.popup(), key, val);
        } else if (this instanceof StageAdapter adapter && adapter.stage() != null) {
            PropertiesUtil.set(adapter.stage(), key, val);
        }else if (this instanceof EventTarget node) {
            PropertiesUtil.set(node, key, val);
        }
    }

    /**
     * 获取属性
     *
     * @param key 键
     * @return 值
     */
    default <T> T getProp(String key) {
        if (this instanceof PopupAdapter adapter) {
            return (T) PropertiesUtil.get(adapter.popup(), key);
        }
        if (this instanceof StageAdapter adapter && adapter.stage() != null) {
            return (T) PropertiesUtil.get(adapter.stage(), key);
        }
        if (this instanceof EventTarget node) {
            return (T) PropertiesUtil.get(node, key);
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
        if (this instanceof PopupAdapter obj && obj.popup() != null) {
            return PropertiesUtil.has(obj.popup(), key);
        }
        if (this instanceof StageAdapter obj && obj.stage() != null) {
            return PropertiesUtil.has(obj.stage(), key);
        }
        if (this instanceof EventTarget obj) {
            return PropertiesUtil.has(obj, key);
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
        if (this instanceof PopupAdapter adapter && adapter.popup() != null) {
            return (T) PropertiesUtil.remove(adapter.popup(), key);
        }
        if (this instanceof StageAdapter adapter && adapter.stage() != null) {
            return (T) PropertiesUtil.remove(adapter.stage(), key);
        }
        if (this instanceof EventTarget node) {
            return (T) PropertiesUtil.remove(node, key);
        }
        return null;
    }

    /**
     * 清除属性
     */
    default void clearProps() {
        if (this instanceof PopupAdapter adapter && adapter.popup() != null) {
            PropertiesUtil.clear(adapter.popup());
        } else if (this instanceof StageAdapter adapter && adapter.stage() != null) {
            PropertiesUtil.clear(adapter.stage());
        } else if (this instanceof EventTarget node) {
            PropertiesUtil.clear(node);
        }
    }
}
