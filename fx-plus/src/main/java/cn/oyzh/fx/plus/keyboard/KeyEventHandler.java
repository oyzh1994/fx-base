package cn.oyzh.fx.plus.keyboard;

import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

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
    public KeyHandler getKeyHandler( KeyCode keyCode,  EventType<KeyEvent> keyType) {
        for (KeyHandler handler : this.handlers) {
            if (handler.getKeyCode() == keyCode && Objects.equals(keyType, handler.getKeyType())) {
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
    public void addHandler( KeyCode keyCode,  EventType<KeyEvent> keyType,  EventHandler<? super KeyEvent> handler) {
        KeyHandler keyHandler = this.getKeyHandler(keyCode, keyType);
        if (keyHandler == null) {
            keyHandler = new KeyHandler();
            keyHandler.setKeyType(keyType);
            keyHandler.setKeyCode(keyCode);
            keyHandler.setHandler(handler);
        } else if (!Objects.equals(handler, keyHandler.getHandler())) {
            keyHandler.setHandler(handler);
        }
        this.handlers.add(keyHandler);
    }

    /**
     * 添加按键处理器
     */
    public void addHandler( KeyHandler keyHandler) {
        this.removeHandler(keyHandler.getKeyCode(), keyHandler.getKeyType());
        this.handlers.add(keyHandler);
    }

    /**
     * 移除按键处理器
     *
     * @param keyCode 按键编码
     * @param keyType 按键类型
     */
    public void removeHandler( KeyCode keyCode,  EventType<KeyEvent> keyType) {
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
                if (event.getCode() != handler.getKeyCode()) {
                    continue;
                }
                if (event.getEventType() != handler.getKeyType()) {
                    continue;
                }
                if (handler.isMetaDown() && !event.isMetaDown()) {
                    continue;
                }
                if (handler.isAltDown() && !event.isAltDown()) {
                    continue;
                }
                if (handler.isShiftDown() && !event.isShiftDown()) {
                    continue;
                }
                if (handler.isControlDown() && !event.isControlDown()) {
                    continue;
                }
                handler.handle(event);
            }
        }
    }
}
