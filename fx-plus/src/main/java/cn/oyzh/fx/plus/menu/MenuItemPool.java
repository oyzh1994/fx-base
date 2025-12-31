package cn.oyzh.fx.plus.menu;

import cn.oyzh.common.util.Pool;
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
        // 尽可能清除属性
        item.setId(null);
        item.idProperty().unbind();
        item.setText(null);
        item.textProperty().unbind();
        item.setStyle(null);
        item.styleProperty().unbind();
        item.setGraphic(null);
        item.graphicProperty().unbind();
        item.setOnAction(null);
        item.onActionProperty().unbind();
        item.setVisible(true);
        item.visibleProperty().unbind();
        item.setDisable(false);
        item.disableProperty().unbind();
        item.setUserData(null);
        item.getProperties().clear();
        item.setOnMenuValidation(null);
        item.setAccelerator(null);
        item.acceleratorProperty().unbind();
        item.mnemonicParsingProperty().unbind();
        super.returnObject(item);
    }
}
