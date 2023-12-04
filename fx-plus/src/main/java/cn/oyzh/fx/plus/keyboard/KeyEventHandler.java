package cn.oyzh.fx.plus.keyboard;

import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import lombok.NonNull;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 按键事件处理器
 *
 * @author oyzh
 * @since 2023/1/16
 */
public class KeyEventHandler implements EventHandler<KeyEvent> {

    /**
     * 按键处理器
     */
    private final List<KeyHandler> handlers = new CopyOnWriteArrayList<>();

    /**
     * 获取按键处理器
     *
     * @param keyCode 按键编码
     * @param keyType 按键类型
     * @return KeyHandler 按键处理器
     */
    public KeyHandler getKeyHandler(@NonNull KeyCode keyCode, @NonNull EventType<KeyEvent> keyType) {
        for (KeyHandler handler : this.handlers) {
            if (handler.keyCode() == keyCode && Objects.equals(keyType, handler.keyType())) {
                return handler;
            }
        }
        return null;
    }

    /**
     * 添加按键处理器
     *
     * @param keyCode 按键编码
     * @param keyType 按键类型
     * @param handler 事件处理器
     */
    public void addHandler(@NonNull KeyCode keyCode, @NonNull EventType<KeyEvent> keyType, @NonNull EventHandler<? super KeyEvent> handler) {
        KeyHandler keyHandler = this.getKeyHandler(keyCode, keyType);
        if (keyHandler == null) {
            keyHandler = new KeyHandler();
            keyHandler.keyType(keyType).keyCode(keyCode).handler(handler);
        } else if (!Objects.equals(handler, keyHandler.handler())) {
            keyHandler.handler(handler);
        }
        this.handlers.add(keyHandler);
    }

    /**
     * 添加按键处理器
     */
    public void addHandler(@NonNull KeyHandler keyHandler) {
        this.removeHandler(keyHandler.keyCode(), keyHandler.keyType());
        this.handlers.add(keyHandler);
    }

    /**
     * 移除按键处理器
     *
     * @param keyCode 按键编码
     * @param keyType 按键类型
     */
    public void removeHandler(@NonNull KeyCode keyCode, @NonNull EventType<KeyEvent> keyType) {
        this.removeHandler(this.getKeyHandler(keyCode, keyType));
    }

    /**
     * 移除按键处理器
     *
     * @param handler 按键处理器
     */
    public void removeHandler(KeyHandler handler) {
        if (handler != null) {
            this.handlers.remove(handler);
        }
    }

    @Override
    public void handle(KeyEvent event) {
        if (!this.handlers.isEmpty()) {
            for (KeyHandler handler : this.handlers) {
                if (event.getCode() != handler.keyCode()) {
                    continue;
                }
                if (event.getEventType() != handler.keyType()) {
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
}
