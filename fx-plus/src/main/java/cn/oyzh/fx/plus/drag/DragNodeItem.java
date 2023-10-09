package cn.oyzh.fx.plus.drag;

import javafx.scene.Node;
import javafx.scene.effect.Effect;

/**
 * 拖动节点接口
 *
 * @author oyzh
 * @since 2023/9/12
 */
public interface DragNodeItem {

    /**
     * 获取拖动特效
     *
     * @return 拖动特效
     */
    default Effect getDragEffect() {
        return null;
    }

    /**
     * 获取投放特效
     *
     * @return 投放特效
     */
    default Effect getDropEffect() {
        return null;
    }

    /**
     * 清除特效
     */
    default void clearEffect() {
        if (this instanceof Node node) {
            node.setEffect(null);
        }
    }

    /**
     * 初始化拖动特效
     */
    default void initDragEffect() {
        if (this instanceof Node node) {
            node.setEffect(this.getDragEffect());
        }
    }

    /**
     * 初始化投放特效
     */
    default void initDropEffect() {
        if (this instanceof Node node) {
            node.setEffect(this.getDropEffect());
        }
    }

    /**
     * 是否允许拖动或者投放
     */
    default boolean allowDragDrop() {
        return this.allowDrag()||this.allowDrop();
    }

    /**
     * 是否允许拖动
     */
    default boolean allowDrag() {
        return false;
    }

    /**
     * 是否允许投放
     *
     * @return 结果
     */
    default boolean allowDrop() {
        return false;
    }

    /**
     * 是否允许投放节点
     *
     * @param item 被拖动节点
     * @return 结果
     */
    default boolean allowDropNode(DragNodeItem item) {
        return false;
    }

    /**
     * 投放事件
     *
     * @param item 被拖动节点
     */
    default void onDropNode(DragNodeItem item) {
    }
}
