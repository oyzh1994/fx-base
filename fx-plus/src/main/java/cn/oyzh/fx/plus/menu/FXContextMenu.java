package cn.oyzh.fx.plus.menu;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.object.DestroyUtil;
import cn.oyzh.fx.plus.adapter.DestroyAdapter;
import cn.oyzh.fx.plus.adapter.LayoutAdapter;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

import java.util.Collection;
import java.util.List;

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
//        this.getItems().addListener(this.itemsListener);
        this.getItems().addListener(new WeakListChangeListener<>(this.itemsListener));
    }

    public FXContextMenu() {
        super();
    }

    public FXContextMenu(FXMenuItem... items) {
        super();
        this.setItem(items);
    }

    public FXContextMenu(List<? extends MenuItem> items) {
        super();
        this.setItem(items);
    }

    /**
     * 计算菜单宽度
     */
    protected void calcWidth() {
        ObservableList<MenuItem> items = this.getItems();
        if (CollectionUtil.isNotEmpty(items)) {
            double maxWidth = 0.d;
            for (MenuItem item : items) {
                if (item instanceof FXMenuItem menuItem) {
                    double w = menuItem.getWidth();
                    if (w > maxWidth) {
                        maxWidth = w;
                    }
                }
            }
            // 设置宽度
            this.setMaxWidth(maxWidth);
            this.setMinWidth(maxWidth);
            this.setPrefWidth(maxWidth);
        }
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

    public void setItem(MenuItem... items) {
        if (items != null) {
            DestroyUtil.destroy(this.getItems());
            this.getItems().setAll(items);
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
