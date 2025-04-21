package cn.oyzh.fx.gui.tabs;

import cn.oyzh.fx.plus.controls.tab.FXTabPane;
import cn.oyzh.fx.plus.menu.MenuItemAdapter;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.scene.control.Tab;

/**
 * 动态tab面板
 *
 * @author oyzh
 * @since 2023/11/03
 */
public class RichTabPane extends FXTabPane {

//    {
//        super.setTabHeight(25);
//        this.setCache(false);
//        this.setCacheHint(CacheHint.QUALITY);
//        this.initTabPane();
//    }

    @Override
    public void initNode() {
        super.initNode();
        // 右键菜单事件
        this.setOnContextMenuRequested(e -> {
            // 判断是否在tab标签栏范围内
            if (this.getTabMaxHeight() - e.getY() >= 0) {
                Tab tab = this.getSelectedItem();
                if (tab instanceof MenuItemAdapter adapter) {
                    this.showContextMenu(adapter.getMenuItems(), e.getScreenX() - 10, e.getScreenY() - 10);
                }
            } else {
                this.clearContextMenu();
            }
        });
    }

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
        Tab tab = this.getTab(tabClass);
        if (tab != null) {
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
