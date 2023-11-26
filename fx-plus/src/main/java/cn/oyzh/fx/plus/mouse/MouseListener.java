package cn.oyzh.fx.plus.mouse;

import cn.hutool.log.StaticLog;
import cn.oyzh.fx.plus.stage.StageWrapper;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * 键盘按键事件
 *
 * @author oyzh
 * @since 2023/1/16
 */
//@Slf4j
@UtilityClass
public class MouseListener {

    /**
     * 监听按键
     *
     * @param target     事件目标
     * @param mouseHandler 按键处理器
     */
    public static void listen(@NonNull Object target, @NonNull MouseHandler mouseHandler) {
        addHandler(target, mouseHandler);
    }

    /**
     * 取消监听按键
     *
     * @param target     事件目标
     * @param mouseHandler 按键处理器
     */
    public static void unListen(@NonNull EventTarget target, @NonNull MouseHandler mouseHandler) {
        removeHandler(target, mouseHandler);
    }

    /**
     * 添加事件处理器
     *
     * @param target       事件目标
     * @param mouseHandler 按键处理器
     */
    private static void addHandler(Object target, MouseHandler mouseHandler) {
        if (target instanceof StageWrapper view) {
            target = view.root();
        } else if (target instanceof Stage stage) {
            target = stage.getScene();
        }
        MouseEventHandler eventHandler = getEventHandler(target);
        if (eventHandler == null) {
            if (target instanceof Scene scene) {
                eventHandler = new MouseEventHandler();
                scene.setOnMouseClicked(eventHandler);
                scene.setOnMouseExited(eventHandler);
                scene.setOnMouseEntered(eventHandler);
                scene.setOnMousePressed(eventHandler);
                scene.setOnMouseReleased(eventHandler);
                scene.setOnMouseDragged(eventHandler);
            } else if (target instanceof Node node) {
                eventHandler = new MouseEventHandler();
                node.setOnMouseClicked(eventHandler);
                node.setOnMouseExited(eventHandler);
                node.setOnMouseEntered(eventHandler);
                node.setOnMousePressed(eventHandler);
                node.setOnMouseReleased(eventHandler);
                node.setOnMouseDragged(eventHandler);
            }
        }
        if (eventHandler != null) {
            eventHandler.addHandler(mouseHandler);
//            if (log.isDebugEnabled()) {
                StaticLog.debug("addHandler, button:{} clickCount:{}", mouseHandler.button(), mouseHandler.clickCount());
//            }
        }
    }

    /**
     * 添加事件处理器
     *
     * @param target  事件目标
     * @param type    按键类型
     * @param button  鼠标按钮
     * @param handler 事件业务处理
     */
    private static void addHandler(EventTarget target, EventType<MouseEvent> type, MouseButton button, EventHandler<? super MouseEvent> handler) {
        addHandler(target, new MouseHandler().type(type).button(button).handler(handler));
    }

    /**
     * 移除事件处理器
     *
     * @param target       事件目标
     * @param mouseHandler 按键处理器
     */
    private static void removeHandler(EventTarget target, MouseHandler mouseHandler) {
        MouseEventHandler eventHandler = getEventHandler(target);
        if (eventHandler != null) {
            eventHandler.removeHandler(mouseHandler);
//            if (log.isDebugEnabled()) {
                StaticLog.debug("removeMouseEventHandler, button:{} clickCount:{}", mouseHandler.button(), mouseHandler.clickCount());
//            }
        }
    }

    /**
     * 获取鼠标按键事件处理器
     *
     * @param target 事件目标
     * @return 鼠标按键事件处理器
     */
    private static MouseEventHandler getEventHandler(Object target) {
        MouseEventHandler eventHandler = null;
        if (target instanceof Scene scene) {
            if (scene.getOnMouseClicked() instanceof MouseEventHandler mouseEventHandler) {
                eventHandler = mouseEventHandler;
            } else if (scene.getOnMousePressed() instanceof MouseEventHandler mouseEventHandler) {
                eventHandler = mouseEventHandler;
            } else if (scene.getOnMouseReleased() instanceof MouseEventHandler mouseEventHandler) {
                eventHandler = mouseEventHandler;
            } else if (scene.getOnMouseEntered() instanceof MouseEventHandler mouseEventHandler) {
                eventHandler = mouseEventHandler;
            } else if (scene.getOnMouseExited() instanceof MouseEventHandler mouseEventHandler) {
                eventHandler = mouseEventHandler;
            } else if (scene.getOnMouseMoved() instanceof MouseEventHandler mouseEventHandler) {
                eventHandler = mouseEventHandler;
            } else if (scene.getOnMouseDragged() instanceof MouseEventHandler mouseEventHandler) {
                eventHandler = mouseEventHandler;
            }
        } else if (target instanceof Node node) {
            if (node.getOnMouseClicked() instanceof MouseEventHandler mouseEventHandler) {
                eventHandler = mouseEventHandler;
            } else if (node.getOnMousePressed() instanceof MouseEventHandler mouseEventHandler) {
                eventHandler = mouseEventHandler;
            } else if (node.getOnMouseReleased() instanceof MouseEventHandler mouseEventHandler) {
                eventHandler = mouseEventHandler;
            } else if (node.getOnMouseEntered() instanceof MouseEventHandler mouseEventHandler) {
                eventHandler = mouseEventHandler;
            } else if (node.getOnMouseExited() instanceof MouseEventHandler mouseEventHandler) {
                eventHandler = mouseEventHandler;
            } else if (node.getOnMouseMoved() instanceof MouseEventHandler mouseEventHandler) {
                eventHandler = mouseEventHandler;
            } else if (node.getOnMouseDragged() instanceof MouseEventHandler mouseEventHandler) {
                eventHandler = mouseEventHandler;
            }
        }
        return eventHandler;
    }
}
