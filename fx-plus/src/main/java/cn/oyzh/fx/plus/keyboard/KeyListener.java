package cn.oyzh.fx.plus.keyboard;

import cn.hutool.log.StaticLog;
import cn.oyzh.fx.plus.stage.StageWrapper;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
public class KeyListener {

    /**
     * 监听按键
     *
     * @param target     事件目标
     * @param keyHandler 按键处理器
     */
    public static void listen(@NonNull Object target, @NonNull KeyHandler keyHandler) {
        addHandler(target, keyHandler);
    }

    /**
     * 取消监听按键
     *
     * @param target     事件目标
     * @param keyHandler 按键处理器
     */
    public static void unListen(@NonNull Object target, @NonNull KeyHandler keyHandler) {
        addHandler(target, keyHandler);
    }

    /**
     * 监听按键
     *
     * @param target  事件目标
     * @param handler 按键处理器
     */
    public static void listenReleased(@NonNull Object target, @NonNull KeyHandler handler) {
        addHandler(target, handler.keyType(KeyEvent.KEY_RELEASED));
    }

    /**
     * 监听按键
     *
     * @param target  事件目标
     * @param keyCode 按键编码
     * @param handler 事件处理器
     */
    public static void listenReleased(@NonNull Object target, @NonNull KeyCode keyCode, @NonNull EventHandler<? super KeyEvent> handler) {
        addHandler(target, new KeyHandler().keyType(KeyEvent.KEY_RELEASED).keyCode(keyCode).handler(handler));
    }

    /**
     * 取消监听按键
     *
     * @param target  事件目标
     * @param keyCode 按键编码
     */
    public static void unListenReleased(@NonNull Object target, @NonNull KeyCode keyCode) {
        removeHandler(target, KeyEvent.KEY_RELEASED, keyCode);
    }

    /**
     * 添加事件处理器
     *
     * @param target     事件目标
     * @param keyHandler 按键处理器
     */
    private static void addHandler(Object target, KeyHandler keyHandler) {
        if (target instanceof StageWrapper wrapper) {
            target = wrapper.root();
        } else if (target instanceof Stage stage) {
            target = stage.getScene();
        }
        KeyEventHandler eventHandler = getEventHandler(target);
        if (eventHandler == null) {
            if (target instanceof Scene scene) {
                eventHandler = new KeyEventHandler();
                scene.setOnKeyTyped(eventHandler);
                scene.setOnKeyPressed(eventHandler);
                scene.setOnKeyReleased(eventHandler);
            } else if (target instanceof Node node) {
                eventHandler = new KeyEventHandler();
                node.setOnKeyTyped(eventHandler);
                node.setOnKeyPressed(eventHandler);
                node.setOnKeyReleased(eventHandler);
            }
        }
        if (eventHandler != null) {
            eventHandler.addHandler(keyHandler);
            StaticLog.debug("addKeyEventHandler, keyType:{} keyCode:{}", keyHandler.keyCode(), keyHandler.keyCode());
        }
    }

    /**
     * 移除事件处理器
     *
     * @param target     事件目标
     * @param keyHandler 按键处理器
     */
    private static void removeHandler(EventTarget target, KeyHandler keyHandler) {
        KeyEventHandler eventHandler = getEventHandler(target);
        if (eventHandler != null) {
            eventHandler.removeHandler(keyHandler.keyCode(), keyHandler.keyType());
            StaticLog.debug("removeHandler, keyType:{} keyCode:{}", keyHandler.keyType(), keyHandler.keyCode());
        }
    }

    /**
     * 移除事件处理器
     *
     * @param target  事件目标
     * @param keyType 按键类型
     * @param keyCode 按键编码
     */
    private static void removeHandler(Object target, EventType<KeyEvent> keyType, KeyCode keyCode) {
        KeyEventHandler eventHandler = getEventHandler(target);
        if (eventHandler != null) {
            eventHandler.removeHandler(keyCode, keyType);
            StaticLog.debug("removeHandler, keyType:{} keyCode:{}", keyType, keyCode);
        }
    }

    /**
     * 获取按键事件处理器
     *
     * @param target 事件目标
     * @return 按键事件处理器
     */
    private static KeyEventHandler getEventHandler(Object target) {
        KeyEventHandler eventHandler = null;
        if (target instanceof Scene scene) {
            if (scene.getOnKeyReleased() instanceof KeyEventHandler keyEventHandler) {
                eventHandler = keyEventHandler;
            } else if (scene.getOnKeyTyped() instanceof KeyEventHandler keyEventHandler) {
                eventHandler = keyEventHandler;
            } else if (scene.getOnKeyPressed() instanceof KeyEventHandler keyEventHandler) {
                eventHandler = keyEventHandler;
            }
        } else if (target instanceof Node node) {
            if (node.getOnKeyReleased() instanceof KeyEventHandler keyEventHandler) {
                eventHandler = keyEventHandler;
            } else if (node.getOnKeyTyped() instanceof KeyEventHandler keyEventHandler) {
                eventHandler = keyEventHandler;
            } else if (node.getOnKeyPressed() instanceof KeyEventHandler keyEventHandler) {
                eventHandler = keyEventHandler;
            }
        }
        return eventHandler;
    }
}
