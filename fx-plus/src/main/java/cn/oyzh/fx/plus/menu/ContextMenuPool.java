//package cn.oyzh.fx.plus.menu;
//
//import cn.oyzh.common.util.Pool;
//import javafx.scene.control.ContextMenu;
//
///**
// * @author oyzh
// * @since 2025-06-25
// */
//public class ContextMenuPool extends Pool<ContextMenu> {
//
//    public ContextMenuPool() {
//        super(1, 3);
//    }
//
//    @Override
//    protected ContextMenu newObject() {
//        return new FXContextMenu();
//    }
//
//    @Override
//    public void returnObject(ContextMenu contextMenu) {
//        if (contextMenu == null) {
//            return;
//        }
//        // 尽可能清楚属性
//        contextMenu.hide();
//        contextMenu.setId(null);
//        contextMenu.idProperty().unbind();
//        contextMenu.setStyle(null);
//        contextMenu.styleProperty().unbind();
//        contextMenu.setOpacity(1);
//        contextMenu.opacityProperty().unbind();
//        contextMenu.setOnAction(null);
//        contextMenu.onActionProperty().unbind();
//        contextMenu.setOnShown(null);
//        contextMenu.onShownProperty().unbind();
//        contextMenu.setOnHidden(null);
//        contextMenu.onHiddenProperty().unbind();
//        contextMenu.setOnShowing(null);
//        contextMenu.onShowingProperty().unbind();
//        contextMenu.getItems().clear();
//        contextMenu.setOnAutoHide(null);
//        contextMenu.onAutoHideProperty().unbind();
//        contextMenu.setOnCloseRequest(null);
//        contextMenu.onCloseRequestProperty().unbind();
//        contextMenu.setUserData(null);
//        contextMenu.getProperties().clear();
//        super.returnObject(contextMenu);
//    }
//}
