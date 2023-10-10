package cn.oyzh.fx.plus.mouse;

import javafx.event.EventHandler;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 鼠标事件处理器
 *
 * @author oyzh
 * @since 2023/10/10
 */
public class MouseEventHandler implements EventHandler<MouseEvent> {

    /**
     * 按键处理器
     */
    private final List<MouseHandler> handlers = new ArrayList<>();

    /**
     * 获取按键处理器
     *
     * @param button     鼠标按钮
     * @param clickCount 点击次数
     * @return KeyHandler 按键处理器
     */
    public MouseHandler getMouseHandler(@NonNull MouseButton button, int clickCount) {
        for (MouseHandler handler : this.handlers) {
            if (handler.button() == button && handler.clickCount() == clickCount) {
                return handler;
            }
        }
        return null;
    }

    /**
     * 添加鼠标按键处理器
     *
     * @param button     鼠标按钮
     * @param clickCount 点击次数
     * @param handler    事件处理器
     */
    public void addHandler(@NonNull MouseButton button, int clickCount, @NonNull EventHandler<? super MouseEvent> handler) {
        MouseHandler mouseHandler = this.getMouseHandler(button, clickCount);
        if (mouseHandler == null) {
            mouseHandler = new MouseHandler();
            mouseHandler.button(button).clickCount(clickCount).handler(handler);
        } else if (!Objects.equals(handler, mouseHandler.handler())) {
            mouseHandler.handler(handler);
        }
        this.handlers.add(mouseHandler);
    }

    /**
     * 添加鼠标按键处理器
     */
    public void addHandler(@NonNull MouseHandler mouseHandler) {
        this.removeHandler(mouseHandler.button(), mouseHandler.clickCount());
        this.handlers.add(mouseHandler);
    }

    /**
     * 移除按鼠标键处理器
     *
     * @param button     鼠标按钮
     * @param clickCount 点击次数
     */
    public void removeHandler(@NonNull MouseButton button, int clickCount) {
        this.removeHandler(this.getMouseHandler(button, clickCount));
    }

    /**
     * 移除鼠标按键处理器
     *
     * @param handler 鼠标按键处理器
     */
    public void removeHandler(MouseHandler handler) {
        if (handler != null) {
            this.handlers.remove(handler);
        }
    }

    @Override
    public void handle(MouseEvent event) {
        if (this.handlers.isEmpty()) {
            return;
        }
        for (MouseHandler handler : this.handlers) {
            if (event.getButton() != handler.button()) {
                continue;
            }
            if (event.getClickCount() < handler.clickCount()) {
                continue;
            }
            if (handler.metaDown() && !event.isMetaDown()) {
                continue;
            }
            if (handler.altDown() && !event.isAltDown()) {
                continue;
            }
            if (handler.shiftDown() && !event.isShiftDown()) {
                continue;
            }
            if (handler.controlDown() && !event.isControlDown()) {
                continue;
            }
            handler.handle(event);
        }
    }
}
