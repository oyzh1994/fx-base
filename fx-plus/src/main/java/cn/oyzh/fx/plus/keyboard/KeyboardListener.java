package cn.oyzh.fx.plus.keyboard;

import cn.oyzh.fx.plus.view.FXView;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

/**
 * 键盘按键事件
 *
 * @author oyzh
 * @since 2023/1/16
 */
@Slf4j
@UtilityClass
public class KeyboardListener {

    /**
     * 监听按键
     *
     * @param target     事件目标
     * @param keyHandler 按键处理器
     */
    public static void listenKey(@NonNull Object target, @NonNull cn.oyzh.fx.plus.keyboard.KeyHandler keyHandler) {
        addKeyEventHandler(target, keyHandler);
    }

    /**
     * 取消监听按键
     *
     * @param target     事件目标
     * @param keyHandler 按键处理器
     */
    public static void unListenKey(@NonNull EventTarget target, @NonNull cn.oyzh.fx.plus.keyboard.KeyHandler keyHandler) {
        removeKeyEventHandler(target, keyHandler);
    }

    /**
     * 监听按键
     *
     * @param target  事件目标
     * @param keyCode 按键编码
     * @param handler 事件处理器
     */
    public static void listenKeyReleased(@NonNull EventTarget target, @NonNull KeyCode keyCode, @NonNull Consumer<KeyEvent> handler) {
        addKeyEventHandler(target, KeyEvent.KEY_RELEASED, keyCode, handler);
    }

    /**
     * 取消监听按键
     *
     * @param target  事件目标
     * @param keyCode 按键编码
     */
    public static void unListenKeyReleased(@NonNull EventTarget target, @NonNull KeyCode keyCode) {
        removeKeyEventHandler(target, KeyEvent.KEY_RELEASED, keyCode);
    }

    ///**
    // * 监听tab按键
    // *
    // * @param view 页面
    // */
    //public static void listenTabKeyReleased(@NonNull FXView view) {
    //    listenKeyReleased(view.getStage(), KeyCode.TAB, e -> FormUtil.toNext(view.getStage(), e));
    //}
    //
    ///**
    // * 取消监听esc按键
    // *
    // * @param view 页面
    // */
    //public static void unListenTabKeyReleased(@NonNull FXView view) {
    //    unListenKeyReleased(view.getStage(), KeyCode.TAB);
    //}

    ///**
    // * 监听esc按键
    // *
    // * @param fxStage 页面
    // */
    //public static void listenEscKeyReleased(@NonNull FXStage fxStage) {
    //    listenKeyReleased(fxStage.getStage(), KeyCode.ESCAPE, event -> fxStage.close());
    //}
    //
    ///**
    // * 取消监听esc按键
    // *
    // * @param fxStage 页面
    // */
    //public static void unListenEscKeyReleased(@NonNull FXStage fxStage) {
    //    unListenKeyReleased(fxStage.getStage(), KeyCode.ESCAPE);
    //}

    /**
     * 添加事件处理器
     *
     * @param target     事件目标
     * @param keyHandler 按键处理器
     */
    private static void addKeyEventHandler(Object target, cn.oyzh.fx.plus.keyboard.KeyHandler keyHandler) {
        if (target instanceof FXView view) {
            target = view.root();
        } else if (target instanceof Stage stage) {
            target = stage.getScene();
        }
        KeyEventHandler eventHandler = getKeyEventHandler(target);
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
            if (log.isDebugEnabled()) {
                log.debug("addKeyEventHandler, keyType:{} keyCode:{}", keyHandler.keyCode(), keyHandler.keyCode());
            }
        }
    }

    /**
     * 添加事件处理器
     *
     * @param target  事件目标
     * @param keyType 按键类型
     * @param keyCode 按键编码
     * @param handler 事件业务处理
     */
    private static void addKeyEventHandler(EventTarget target, EventType<KeyEvent> keyType, KeyCode keyCode, Consumer<KeyEvent> handler) {
        addKeyEventHandler(target, new cn.oyzh.fx.plus.keyboard.KeyHandler().keyType(keyType).keyCode(keyCode).handler(handler));
    }

    /**
     * 移除事件处理器
     *
     * @param target     事件目标
     * @param keyHandler 按键处理器
     */
    private static void removeKeyEventHandler(EventTarget target, KeyHandler keyHandler) {
        KeyEventHandler eventHandler = getKeyEventHandler(target);
        if (eventHandler != null) {
            eventHandler.removeHandler(keyHandler.keyCode(), keyHandler.keyType());
            if (log.isDebugEnabled()) {
                log.debug("removeKeyEventHandler, keyType:{} keyCode:{}", keyHandler.keyType(), keyHandler.keyCode());
            }
        }
    }

    /**
     * 移除事件处理器
     *
     * @param target  事件目标
     * @param keyType 按键类型
     * @param keyCode 按键编码
     */
    private static void removeKeyEventHandler(EventTarget target, EventType<KeyEvent> keyType, KeyCode keyCode) {
        KeyEventHandler eventHandler = getKeyEventHandler(target);
        if (eventHandler != null) {
            eventHandler.removeHandler(keyCode, keyType);
            if (log.isDebugEnabled()) {
                log.debug("removeKeyEventHandler, keyType:{} keyCode:{}", keyType, keyCode);
            }
        }
    }

    /**
     * 获取按键事件处理器
     *
     * @param target 事件目标
     * @return 按键事件处理器
     */
    private static KeyEventHandler getKeyEventHandler(Object target) {
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
