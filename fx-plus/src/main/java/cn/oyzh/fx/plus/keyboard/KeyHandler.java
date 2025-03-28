package cn.oyzh.fx.plus.keyboard;

import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * 键盘按键处理器
 *
 * @author oyzh
 * @since 2023/2/8
 */
public class KeyHandler {

    /**
     * 按键编码
     */
    private KeyCode keyCode;

    /**
     * 是否alt按下
     */
    private boolean altDown;

    /**
     * 是否meta按下
     */
    private boolean metaDown;

    /**
     * 是否shift按下
     */
    private boolean shiftDown;

    /**
     * 是否control按下
     */
    private boolean controlDown;

    /**
     * 按键类型
     */
    private EventType<KeyEvent> keyType;

    /**
     * 事件处理器
     */
    private EventHandler<? super KeyEvent> handler;

    public void handle(KeyEvent event) {
        if (this.handler != null) {
            this.handler.handle(event);
        }
    }

    public KeyCode getKeyCode() {
        return keyCode;
    }

    public void setKeyCode(KeyCode keyCode) {
        this.keyCode = keyCode;
    }

    public boolean isAltDown() {
        return altDown;
    }

    public void setAltDown(boolean altDown) {
        this.altDown = altDown;
    }

    public boolean isMetaDown() {
        return metaDown;
    }

    public void setMetaDown(boolean metaDown) {
        this.metaDown = metaDown;
    }

    public boolean isShiftDown() {
        return shiftDown;
    }

    public void setShiftDown(boolean shiftDown) {
        this.shiftDown = shiftDown;
    }

    public boolean isControlDown() {
        return controlDown;
    }

    public void setControlDown(boolean controlDown) {
        this.controlDown = controlDown;
    }

    public EventType<KeyEvent> getKeyType() {
        return keyType;
    }

    public void setKeyType(EventType<KeyEvent> keyType) {
        this.keyType = keyType;
    }

    public EventHandler<? super KeyEvent> getHandler() {
        return handler;
    }

    public void setHandler(EventHandler<? super KeyEvent> handler) {
        this.handler = handler;
    }
}
