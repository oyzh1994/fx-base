package cn.oyzh.fx.plus.keyboard;

import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 键盘按键处理器
 *
 * @author oyzh
 * @since 2023/2/8
 */
@Data
@Accessors(fluent = true, chain = true)
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
}
