package cn.oyzh.fx.plus.adapter;

import javafx.scene.Node;
import javafx.scene.control.ScrollBar;

/**
 *
 * @author oyzh
 * @since 2025-09-12
 */
public interface ScrollBarAdapter {

    /**
     * 获取滚动条
     *
     * @return 滚动条
     */
    default ScrollBar getScrollBar() {
        if (this instanceof Node node) {
            return (ScrollBar) node.lookup("ScrollBar");
        }
        return null;
    }

    /**
     * 获取滚动值
     *
     * @return 滚动值
     */
    default Double getScrollValue() {
        ScrollBar scrollBar = this.getScrollBar();
        if (scrollBar != null) {
            return scrollBar.getValue();
        }
        return null;
    }

    /**
     * 设置滚动值
     *
     * @param value 滚动值
     */
    default void setScrollValue(Double value) {
        ScrollBar scrollBar = this.getScrollBar();
        if (scrollBar != null && value != null) {
            scrollBar.setValue(value);
        }
    }
}
