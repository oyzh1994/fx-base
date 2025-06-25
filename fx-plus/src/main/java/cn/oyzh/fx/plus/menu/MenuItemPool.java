package cn.oyzh.fx.plus.menu;

import cn.oyzh.common.util.Pool;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

/**
 * @author oyzh
 * @since 2025-06-25
 */
public class MenuItemPool extends Pool<MenuItem> {

    public MenuItemPool() {
        super(3, 20);
    }

    @Override
    protected MenuItem newObject() {
        return new FXMenuItem();
    }

    @Override
    public void returnObject(MenuItem item) {
        if (item == null) {
            return;
        }
        // 尽可能清楚属性
        item.setId(null);
        item.setText(null);
        item.setStyle(null);
        item.setGraphic(null);
        item.setOnAction(null);
        item.setVisible(true);
        item.setDisable(false);
        item.setUserData(null);
        item.getProperties().clear();
        item.disableProperty().unbind();
        item.setOnMenuValidation(null);
        super.returnObject(item);
    }
}
