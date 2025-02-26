package cn.oyzh.fx.gui.tabs;

import cn.oyzh.event.Event;
import javafx.scene.control.Tab;

/**
 * @author oyzh
 * @since 2024-10-16
 */
public class TabClosedEvent extends Event<Tab> {

    public TabClosedEvent(Tab tab) {
        super(tab);
    }
}
