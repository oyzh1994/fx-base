package cn.oyzh.fx.plus.mouse;

import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import lombok.Data;
import lombok.experimental.Accessors;


/**
 * 鼠标按键处理器
 *
 * @author oyzh
 * @since 2023/10/10
 */
@Data
@Accessors(fluent = true, chain = true)
public class MouseHandler {

    /**
     * 鼠标按钮
     */
    private MouseButton button;

    /**
     * 按键类型
     */
    private EventType<MouseEvent> type;

    /**
     * 事件处理器
     */
    private EventHandler<? super MouseEvent> handler;

    /**
     * 点击次数
     */
    private Integer clickCount;

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

    public void handle(MouseEvent event) {
        this.handler.handle(event);
    }
}
