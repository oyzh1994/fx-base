package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.common.util.CollectionUtil;
import cn.oyzh.fx.common.util.DestroyUtil;
import cn.oyzh.fx.plus.adapter.DestroyAdapter;
import cn.oyzh.fx.plus.adapter.LayoutAdapter;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.collections.ListChangeListener;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

import java.util.Collection;

/**
 * @author oyzh
 * @since 2023/3/7
 */
public class FXContextMenu extends ContextMenu implements LayoutAdapter, ThemeAdapter, DestroyAdapter {

    {
        NodeManager.init(this);
    }

    private ListChangeListener<MenuItem> itemsListener = c -> this.calcWidth();

    {
        this.setStyle("-fx-padding: 0 0 0 0;");
        this.getItems().addListener(this.itemsListener);
    }

    public FXContextMenu() {
        super();
    }

    public FXContextMenu(FXMenuItem... items) {
        super();
        this.getItems().addAll(items);
    }

    /**
     * 计算菜单宽度
     */
    private void calcWidth() {
        if (CollectionUtil.isNotEmpty(this.getItems())) {
            double maxWidth = 0.d;
            for (MenuItem item : this.getItems()) {
                if (item instanceof FXMenuItem item1 && item1.getWidth() > maxWidth) {
                    maxWidth = item1.getWidth();
                }
            }
            // 设置宽度
            this.setWidth(maxWidth);
            this.setRealWidth(maxWidth);
        }
    }

    @Override
    public double getRealWidth() {
        return LayoutAdapter.super.realWidth();
    }

    @Override
    public void setRealWidth(double width) {
        LayoutAdapter.super.realWidth(width);
    }

    @Override
    public double getRealHeight() {
        return LayoutAdapter.super.realHeight();
    }

    @Override
    public void setRealHeight(double height) {
        LayoutAdapter.super.realHeight(height);
    }

    public void addItem(MenuItem item) {
        if (item != null) {
            this.getItems().add(item);
        }
    }

    public void setItem(MenuItem item) {
        if (item != null) {
            this.getItems().setAll(item);
        }
    }

    public void setItem(Collection<? extends MenuItem> items) {
        if (items != null) {
            DestroyUtil.destroy(this.getItems());
            this.getItems().setAll(items);
        }
    }

    public void cleaItem() {
        DestroyUtil.destroy(this.getItems());
        this.getItems().clear();
    }

    @Override
    public void destroy() {
        this.getItems().removeListener(this.itemsListener);
        this.cleaItem();
        this.itemsListener = null;
        this.setStyle(null);
        this.clearProps();
    }
}
