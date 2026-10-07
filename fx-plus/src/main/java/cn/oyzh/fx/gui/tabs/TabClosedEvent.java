package cn.oyzh.fx.gui.tabs;

import cn.oyzh.event.Event;
import javafx.scene.control.Tab;

/**
 * 标签页关闭事件
 *
 * @author oyzh
 * @since 2024-10-16
 */
public class TabClosedEvent extends Event<Tab> {

    /**
     * 构造标签页关闭事件
     *
     * @param tab 已关闭的标签页
     */
    public TabClosedEvent(Tab tab) {
        super(tab);
    }
}
