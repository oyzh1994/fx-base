package cn.oyzh.fx.plus.tray;

import cn.oyzh.fx.plus.util.FXUtil;
import dorkbox.systemTray.MenuItem;
import javafx.scene.Node;

/**
 * @author oyzh
 * @since 2025-08-19
 */
public class DorkboxTrayItem extends MenuItem implements BaseTrayItem {

    public DorkboxTrayItem(String label, Runnable action) {
        super(label, e -> action.run());
    }

    @Override
    public DorkboxTrayItem toDorkboxTrayItem() {
        return this;
    }
}
