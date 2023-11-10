package cn.oyzh.fx.plus.controls.tabs;

import cn.oyzh.fx.plus.controls.FlexTabPane;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.scene.CacheHint;
import javafx.scene.control.Tab;

/**
 * 动态tab面板
 *
 * @author oyzh
 * @since 2023/11/03
 */
public class DynamicTabPane extends FlexTabPane {

    {
        this.setCache(true);
        this.setCacheHint(CacheHint.QUALITY);
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
        if (this.getSelectedItem() instanceof DynamicTab tab) {
            tab.reload();
        }
    }
}
