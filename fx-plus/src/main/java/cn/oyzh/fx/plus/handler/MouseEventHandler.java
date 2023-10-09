package cn.oyzh.fx.plus.handler;

import cn.oyzh.fx.plus.util.MouseUtil;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 鼠标事件处理器
 *
 * @author oyzh
 * @since 2023/4/17
 */
public class MouseEventHandler implements EventHandler<MouseEvent> {

    /**
     * 主按钮点击事件
     */
    @Setter
    @Getter
    @Accessors(chain = true, fluent = true)
    private EventHandler<? super MouseEvent> primaryClicked;

    /**
     * 次按钮点击事件
     */
    @Setter
    @Getter
    @Accessors(chain = true, fluent = true)
    private EventHandler<? super MouseEvent> secondClicked;

    @Override
    public void handle(MouseEvent event) {
        if (MouseUtil.isPrimaryButton(event)) {
            if (this.primaryClicked != null) {
                this.primaryClicked.handle(event);
            }
        } else if (MouseUtil.isSecondButton(event)) {
            if (this.secondClicked != null) {
                this.secondClicked.handle(event);
            }
        }
    }
}
