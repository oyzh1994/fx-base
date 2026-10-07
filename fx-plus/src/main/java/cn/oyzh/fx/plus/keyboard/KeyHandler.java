package cn.oyzh.fx.plus.keyboard;

import cn.oyzh.common.system.OSUtil;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * 键盘按键处理器
 *
 * @author oyzh
 * @since 2023-10-09
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

    /**
     * 触发事件处理器
     *
     * @param event 按键事件
     */
    public void handle(KeyEvent event) {
        if (this.handler != null) {
            this.handler.handle(event);
        }
    }

    /**
     * 获取键码。
     *
     * @return 键码
     */
    public KeyCode getKeyCode() {
        return keyCode;
    }

    /**
     * 设置键码。
     *
     * @param keyCode 键码
     */
    public void setKeyCode(KeyCode keyCode) {
        this.keyCode = keyCode;
    }

    /**
     * 是否Alt按下。
     *
     * @return Alt按下
     */
    public boolean isAltDown() {
        return altDown;
    }

    /**
     * 设置Alt按下。
     *
     * @param altDown Alt是否按下
     */
    public void setAltDown(boolean altDown) {
        this.altDown = altDown;
    }

    /**
     * 是否Meta按下。
     *
     * @return Meta按下
     */
    public boolean isMetaDown() {
        return metaDown;
    }

    /**
     * 设置Meta按下。
     *
     * @param metaDown Meta是否按下
     */
    public void setMetaDown(boolean metaDown) {
        this.metaDown = metaDown;
    }

    /**
     * 是否Shift按下。
     *
     * @return Shift按下
     */
    public boolean isShiftDown() {
        return shiftDown;
    }

    /**
     * 设置Shift按下。
     *
     * @param shiftDown Shift是否按下
     */
    public void setShiftDown(boolean shiftDown) {
        this.shiftDown = shiftDown;
    }

    /**
     * 是否控件按下。
     *
     * @return 控件按下
     */
    public boolean isControlDown() {
        return controlDown;
    }

    /**
     * 设置控件按下。
     *
     * @param controlDown Control是否按下
     */
    public void setControlDown(boolean controlDown) {
        this.controlDown = controlDown;
    }

    /**
     * 获取键类型。
     *
     * @return 键类型
     */
    public EventType<KeyEvent> getKeyType() {
        return keyType;
    }

    /**
     * 设置键类型。
     *
     * @param keyType 键类型
     */
    public void setKeyType(EventType<KeyEvent> keyType) {
        this.keyType = keyType;
    }

    /**
     * 获取处理器。
     *
     * @return 处理器
     */
    public EventHandler<? super KeyEvent> getHandler() {
        return handler;
    }

    /**
     * 设置处理器。
     *
     * @param handler 处理器
     */
    public void setHandler(EventHandler<? super KeyEvent> handler) {
        this.handler = handler;
    }

    /**
     * 设置主修饰键是否按下（macOS 对应 meta，其他平台对应 control）
     *
     * @param mainModifierDown 主修饰键是否按下
     */
    public void setMainModifierDown(boolean mainModifierDown) {
        if (OSUtil.isMacOS()) {
            this.setMetaDown(mainModifierDown);
        } else {
            this.setControlDown(mainModifierDown);
        }
    }
}
