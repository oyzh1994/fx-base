package cn.oyzh.fx.plus.adapter;

import cn.oyzh.fx.plus.handler.MouseEventHandler;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

/**
 * 鼠标事件
 *
 * @author oyzh
 * @since 2022/5/12
 */
public interface MouseAdapter extends PropAdapter {

    /**
     * 获取鼠标事件处理器
     *
     * @param initIfNull 如果为空，是否初始化
     * @return 鼠标事件处理器
     */
    private MouseEventHandler getOnMouseEventHandler(boolean initIfNull) {
        if (this instanceof Node node) {
            MouseEventHandler handler = null;
            if (node.getOnMouseClicked() instanceof MouseEventHandler) {
                handler = (MouseEventHandler) node.getOnMouseClicked();
            }
            if (handler == null && initIfNull) {
                handler = new MouseEventHandler();
                node.setOnMouseClicked(handler);
            }
            return handler;
        } else {
            throw new UnsupportedOperationException("must be node!");
        }
    }

    /**
     * 获取鼠标主按钮点击事件
     *
     * @return 事件处理器
     */
    default EventHandler<? super MouseEvent> getOnMousePrimaryClicked() {
        MouseEventHandler handler = this.getOnMouseEventHandler(false);
        return handler == null ? null : handler.primaryClicked();
    }

    /**
     * 设置鼠标主按钮点击事件
     *
     * @param handler 事件处理器
     */
    default void setOnMousePrimaryClicked(EventHandler<? super MouseEvent> handler) {
        this.getOnMouseEventHandler(true).primaryClicked(handler);
    }

    /**
     * 设置鼠标次按钮点击事件
     *
     * @return 事件处理器
     */
    default EventHandler<? super MouseEvent> getOnMouseSecondClicked() {
        MouseEventHandler handler = this.getOnMouseEventHandler(false);
        return handler == null ? null : handler.secondClicked();
    }

    /**
     * 设置鼠标次按钮点击事件
     *
     * @param handler 事件处理器
     */
    default void setOnMouseSecondClicked(EventHandler<? super MouseEvent> handler) {
        this.getOnMouseEventHandler(true).secondClicked(handler);
    }
}
