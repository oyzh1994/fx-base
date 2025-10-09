package cn.oyzh.fx.plus.menu;

import cn.oyzh.common.util.Pool;
import javafx.scene.control.ContextMenu;

/**
 * @author oyzh
 * @since 2025-06-25
 */
public class ContextMenuPool extends Pool<ContextMenu> {

    public ContextMenuPool() {
        super(1, 3);
    }

    @Override
    protected ContextMenu newObject() {
        return new FXContextMenu();
    }

    @Override
    public void returnObject(ContextMenu contextMenu) {
        if (contextMenu == null) {
            return;
        }
        // 尽可能清楚属性
        contextMenu.hide();
        contextMenu.setId(null);
        contextMenu.setStyle(null);
        contextMenu.setOpacity(1);
        contextMenu.setOnAction(null);
        contextMenu.setOnShown(null);
        contextMenu.setOnHidden(null);
        contextMenu.setOnShowing(null);
        contextMenu.getItems().clear();
        contextMenu.setOnAutoHide(null);
        contextMenu.setOnCloseRequest(null);
        contextMenu.getProperties().clear();
        super.returnObject(contextMenu);
    }
}
