package cn.oyzh.fx.plus.adapter;

import cn.oyzh.fx.plus.util.NodeUtil;
import javafx.event.EventTarget;

/**
 * 布局适配器
 *
 * @author oyzh
 * @since 2023/4/11
 */
public interface LayoutAdapter {

    /**
     * 获取真实宽度
     *
     * @return 真实宽度
     */
    double getRealWidth();

    /**
     * 获取真实宽度实现
     *
     * @return 真实宽度
     */
    default double _getRealWidth() {
        if (this instanceof EventTarget target) {
            return NodeUtil.getWidth(target);
        }
        return Double.NaN;
    }

    /**
     * 设置真实宽度
     *
     * @param width 真实宽度
     */
    void setRealWidth(double width);

    /**
     * 设置真实宽度实现
     *
     * @param width 真实宽度
     */
    default void _setRealWidth(double width) {
        if (this instanceof EventTarget target) {
            NodeUtil.setWidth(target, width);
        }
    }

    /**
     * 获取真实高度
     *
     * @return 真实高度
     */
    double getRealHeight();

    /**
     * 获取真实高度实现
     *
     * @return 真实高度
     */
    default double _getRealHeight() {
        if (this instanceof EventTarget target) {
            return NodeUtil.getHeight(target);
        }
        return Double.NaN;
    }

    /**
     * 设置真实高度
     *
     * @param height 真实高度
     */
    void setRealHeight(double height);

    /**
     * 设置真实高度实现
     *
     * @param height 真实高度
     */
    default void _setRealHeight(double height) {
        if (this instanceof EventTarget target) {
            NodeUtil.setHeight(target, height);
        }
    }

    /**
     * 设置宽，全部属性
     *
     * @param width 宽
     */
    default void setWidthAll(double width) {
        if (this instanceof EventTarget target) {
            NodeUtil.setWidth(target, width);
        }
    }

    /**
     * 设置高，全部属性
     *
     * @param height 高度
     */
    default void setHeightAll(double height) {
        if (this instanceof EventTarget target) {
            NodeUtil.setHeight(target, height);
        }
    }

    /**
     * 设置layoutY，全部属性
     *
     * @param layoutY layoutY
     */
    default void setLayoutYAll(double layoutY) {
        if (this instanceof EventTarget target) {
            NodeUtil.setLayoutY(target, layoutY);
        }
    }
}
