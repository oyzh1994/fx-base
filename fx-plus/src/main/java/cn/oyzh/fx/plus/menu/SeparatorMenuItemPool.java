package cn.oyzh.fx.plus.menu;

import cn.oyzh.common.util.Pool;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

/**
 * @author oyzh
 * @since 2025-06-25
 */
public class SeparatorMenuItemPool extends Pool<SeparatorMenuItem> {

    public SeparatorMenuItemPool() {
        super(1, 5);
    }

    @Override
    protected SeparatorMenuItem newObject() throws Exception {
        return new SeparatorMenuItem();
    }
}
