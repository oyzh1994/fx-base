package cn.oyzh.fx.plus.controls;

import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TreeCell;

/**
 * 树列
 *
 * @author oyzh
 * @since 2023/03/31
 */
public abstract class FXTreeCell<T> extends TreeCell<T> implements ThemeAdapter, StateAdapter {

    {
        // this.cacheProperty().bind(this.visibleProperty());
        // this.managedProperty().bind(this.visibleProperty());
        this.setCache(true);
        this.setCacheShape(true);
//        this.setCacheHint(CacheHint.QUALITY);
        this.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }

    /**
     * 初始化图形
     *
     * @return 图形
     */
    public abstract Node initGraphic();

    /**
     * 更新节点信息
     *
     * @param item  节点
     * @param empty 是否为空
     */
    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            this.setGraphic(null);
            // this.hideNode();
        } else {
            Node node = this.initGraphic();
            if (node != this.getGraphic()) {
                this.setGraphic(node);
            }
            // this.showNode();
        }
    }
}
