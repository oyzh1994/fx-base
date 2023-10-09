package cn.oyzh.fx.plus.keyboard;

import javafx.event.EventType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.function.Consumer;

/**
 * 按键处理器
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
     * 事件处理器
     */
    private Consumer<KeyEvent> handler;

    /**
     * 按键类型
     */
    private EventType<KeyEvent> keyType;

    /**
     * 是否alt按下
     */
    private boolean altDown;

    /**
     * 是否 meta按下
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
}
