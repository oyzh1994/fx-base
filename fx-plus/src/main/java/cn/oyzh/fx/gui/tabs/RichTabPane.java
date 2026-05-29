package cn.oyzh.fx.gui.tabs;

import cn.oyzh.fx.plus.controls.tab.FXTabPane;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.scene.control.Tab;

/**
 * 动态tab面板
 *
 * @author oyzh
 * @since 2023/11/03
 */
public class RichTabPane extends FXTabPane {

//    @Override
//    public void initNode() {
//        // 右键菜单事件
//        this.setOnContextMenuRequested(e -> {
//            double pos = 0;
//            if (this.getSide() == Side.TOP) {
//                pos = e.getY();
//                //                pos = this.getTabMaxHeight() - e.getY();
//            } else if (this.getSide() == Side.BOTTOM) {
//                //                pos = e.getY() - this.getHeight() + this.getTabMaxHeight();
//                pos = e.getY() - this.getHeight();
//            }
//            // 判断是否在tab标签栏范围内
//            double h = this.getTabMaxHeight() - this.getTabMinHeight();
//            if (pos >= -h & pos <= h) {
//                Tab tab = this.getSelectedItem();
////                if (tab instanceof MenuItemAdapter adapter) {
////                    this.showContextMenu(adapter.getMenuItems(), e.getScreenX() - 10, e.getScreenY() - 10);
////                }
//
//                e.consume();
//                //            } else {
//                //                this.clearContextMenu();
//            }
//        });
//        super.initNode();
//    }

    /**
     * 获取tab
     *
     * @param tabClass tab类
     * @return tab
     */
    public <T extends Tab> T getTab(Class<? extends T> tabClass) {
        for (Tab tab : this.getTabs()) {
            if (tab.getClass() == tabClass) {
                return (T) tab;
            }
        }
        return null;
    }

    /**
     * 关闭tab
     *
     * @param tabClass tab类
     */
    public void closeTab(Class<? extends Tab> tabClass) {
        // Tab tab = this.getTab(tabClass);
        // if (tab != null) {
        //     FXUtil.runLater(() -> this.getTabs().remove(tab));
        // }
        this.closeTab(this.getTab(tabClass));
    }

    /**
     * 关闭tab
     *
     * @param tab tab类
     */
    public void closeTab(Tab tab) {
        if (tab != null && tab.isClosable()) {
            FXUtil.runLater(() -> this.getTabs().remove(tab));
        }
    }

    /**
     * 重新载入
     */
    public void reload() {
        if (this.getSelectedItem() instanceof RichTab tab) {
            tab.reload();
        }
    }

}
