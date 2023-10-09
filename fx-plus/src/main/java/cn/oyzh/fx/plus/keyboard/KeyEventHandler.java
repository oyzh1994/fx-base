package cn.oyzh.fx.plus.keyboard;

import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * 按键事件处理器
 *
 * @author oyzh
 * @since 2023/1/16
 */
public class KeyEventHandler implements EventHandler<KeyEvent> {

    ///**
    // * 事件目标对象
    // */
    //private final Object target;

    /**
     * 按键处理器
     */
    private final List<cn.oyzh.fx.plus.keyboard.KeyHandler> handlers = new ArrayList<>();

    //public KeyEventHandler(@NonNull Object target) {
    //    this.target = target;
    //}

    /**
     * 获取按键处理器
     *
     * @param keyCode 按键编码
     * @param keyType 按键类型
     * @return KeyHandler 按键处理器
     */
    public cn.oyzh.fx.plus.keyboard.KeyHandler getKeyHandler(@NonNull KeyCode keyCode, @NonNull EventType<KeyEvent> keyType) {
        for (cn.oyzh.fx.plus.keyboard.KeyHandler handler : this.handlers) {
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
    public void addHandler(@NonNull KeyCode keyCode, @NonNull EventType<KeyEvent> keyType, @NonNull Consumer<KeyEvent> handler) {
        cn.oyzh.fx.plus.keyboard.KeyHandler keyHandler = this.getKeyHandler(keyCode, keyType);
        if (keyHandler == null) {
            keyHandler = new cn.oyzh.fx.plus.keyboard.KeyHandler();
            keyHandler.keyType(keyType).keyCode(keyCode).handler(handler);
        } else if (!Objects.equals(handler, keyHandler.handler())) {
            keyHandler.handler(handler);
        }
        this.handlers.add(keyHandler);
    }

    /**
     * 添加按键处理器
     */
    public void addHandler(@NonNull cn.oyzh.fx.plus.keyboard.KeyHandler keyHandler) {
        cn.oyzh.fx.plus.keyboard.KeyHandler keyHandler1 = this.getKeyHandler(keyHandler.keyCode(), keyHandler.keyType());
        if (keyHandler1 != null) {
            this.removeHandler(keyHandler1);
        }
        this.handlers.add(keyHandler);
    }

    /**
     * 移除按键处理器
     *
     * @param keyCode 按键编码
     * @param keyType 按键类型
     */
    public void removeHandler(@NonNull KeyCode keyCode, @NonNull EventType<KeyEvent> keyType) {
        cn.oyzh.fx.plus.keyboard.KeyHandler keyHandler = getKeyHandler(keyCode, keyType);
        if (keyHandler != null) {
            this.removeHandler(keyHandler);
        }
    }

    /**
     * 移除按键处理器
     *
     * @param handler 按键处理器
     */
    public void removeHandler(@NonNull cn.oyzh.fx.plus.keyboard.KeyHandler handler) {
        this.handlers.remove(handler);
    }

    @Override
    public void handle(KeyEvent event) {
        //// 检查是否获得焦点
        //if (this.target instanceof Window window && !window.isFocused()) {
        //    return;
        //}
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
                handler.handler().accept(event);
            }
        }
    }
}
