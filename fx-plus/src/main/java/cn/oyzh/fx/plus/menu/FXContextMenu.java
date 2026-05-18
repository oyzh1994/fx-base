package cn.oyzh.fx.plus.menu;

import cn.oyzh.common.object.DestroyUtil;
import cn.oyzh.common.object.ObjectWatcherManager;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.fx.plus.adapter.LayoutAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

import java.lang.ref.WeakReference;
import java.util.Collection;

/**
 * @author oyzh
 * @since 2023/3/7
 */
public class FXContextMenu extends ContextMenu implements NodeAdapter, LayoutAdapter, ThemeAdapter {

    private final ListChangeListener<MenuItem> itemsListener = c -> this.calcWidth();

    {
        NodeManager.init(this);
        ObjectWatcherManager.watch(this);
    }

    private WeakReference<Object> targetRef;

    public FXContextMenu(Object target) {
        super();
        this.targetRef = new WeakReference<>(target);
    }

    public void setTarget(Object target) {
        this.targetRef = new WeakReference<>(target);
    }

    private double width;

    /**
     * 计算菜单宽度
     */
    protected void calcWidth() {
        ObservableList<MenuItem> items = this.getItems();
        if (CollectionUtil.isNotEmpty(items)) {
            double width = 0.d;
            for (MenuItem item : items) {
                double w = FXMenuItem.getWidth(item);
                if (w > width) {
                    width = w;
                }
            }
            // 设置宽度
            this.setWidth(width);
            this.width = width;
        } else {
            this.width = Double.NaN;
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
        if (items != null && !this.getItems().equals(items)) {
            DestroyUtil.destroy(this.getItems());
            this.getItems().setAll(items);
        }
    }


    @Override
    public void initNode() {
        this.sizeToScene();
        this.setStyle("-fx-padding: 0 0 0 0;");
        this.getItems().addListener(this.itemsListener);
        this.showingProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                this.calcWidth();
            } else {
                this.clear();
            }
        });
        this.prefWidthProperty().addListener((observable, oldValue, newValue) -> {
            if (!Double.isNaN(this.width) && newValue.doubleValue() != this.width) {
                this.getScene().getWindow().setWidth(this.width);
                this.setPrefWidth(this.width);
            }
        });
        NodeAdapter.super.initNode();
    }

    public void clear() {
        for (MenuItem item : this.getItems()) {
            item.setOnAction(null);
        }
        this.getItems().clear();
        ContextMenuManager.clearContextMenu(this.targetRef.get());
    }
}
