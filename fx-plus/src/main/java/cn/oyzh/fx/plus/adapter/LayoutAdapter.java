package cn.oyzh.fx.plus.adapter;

import cn.oyzh.fx.plus.node.NodeUtil;
import javafx.event.EventTarget;

/**
 * 布局适配器
 *
 * @author oyzh
 * @since 2023/4/11
 */
public interface LayoutAdapter extends EventTarget {

    /**
     * 获取真实宽度
     *
     * @return 真实宽度
     */
    default double getRealWidth() {
        return this.realWidth();
    }

    /**
     * 获取真实宽度实现
     *
     * @return 真实宽度
     */
    default double realWidth() {
        return NodeUtil.getWidth(this);
    }

    /**
     * 设置真实宽度
     *
     * @param width 真实宽度
     */
    default void setRealWidth(double width) {
        this.realWidth(width);
    }

    /**
     * 设置真实宽度实现
     *
     * @param width 真实宽度
     */
    default void realWidth(double width) {
        NodeUtil.setWidth(this, width);
    }

    /**
     * 获取真实高度
     *
     * @return 真实高度
     */
    default double getRealHeight() {
        return this.realHeight();
    }

    /**
     * 获取真实高度实现
     *
     * @return 真实高度
     */
    default double realHeight() {
        return NodeUtil.getHeight(this);
    }

    /**
     * 设置真实高度
     *
     * @param height 真实高度
     */
    default void setRealHeight(double height) {
        this.realHeight(height);
    }

    /**
     * 设置真实高度实现
     *
     * @param height 真实高度
     */
    default void realHeight(double height) {
        NodeUtil.setHeight(this, height);
    }

    /**
     * 设置layoutY，全部属性
     *
     * @param layoutY layoutY
     */
    default void setLayoutY(double layoutY) {
        NodeUtil.setLayoutY(this, layoutY);
    }
}
