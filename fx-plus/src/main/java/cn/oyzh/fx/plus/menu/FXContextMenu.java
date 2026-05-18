package cn.oyzh.fx.plus.menu;

import cn.oyzh.common.object.DestroyUtil;
import cn.oyzh.common.object.Destroyable;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.fx.plus.adapter.LayoutAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeDestroyUtil;
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
public class FXContextMenu extends ContextMenu implements NodeAdapter, LayoutAdapter, ThemeAdapter, Destroyable {

    {
        NodeManager.init(this);
    }

    public static final FXContextMenu EMPTY = new FXContextMenu();

    private ListChangeListener<MenuItem> itemsListener = c -> this.calcWidth();

    {
        this.setStyle("-fx-padding: 0 0 0 0;");
        this.getItems().addListener(this.itemsListener);
//        this.getItems().addListener(new WeakListChangeListener<>(this.itemsListener));
        this.showingProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                this.calcWidth();
            }
        });
        this.prefWidthProperty().addListener((observable, oldValue, newValue) -> {
            if (!Double.isNaN(this.width) && newValue.doubleValue() != this.width) {
                this.getScene().getWindow().setWidth(this.width);
                this.setPrefWidth(this.width);
                System.out.println(this.width);
            }
        });
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

    private double width;

    /**
     * 计算菜单宽度
     */
    protected void calcWidth() {
        ObservableList<MenuItem> items = this.getItems();
        if (CollectionUtil.isNotEmpty(items)) {
            double width = 0.d;
            for (MenuItem item : items) {
                // if (item instanceof FXMenuItem menuItem) {
                //     double w = menuItem.getWidth();
                //     if (w > width) {
                //         width = w;
                //     }
                // }
                double w = FXMenuItem.getWidth(item);
                if (w > width) {
                    width = w;
                }
            }
            // 设置宽度
            this.setWidth(width);
            this.width = width;
            // this.setMaxWidth(width);
            // this.setMinWidth(width);
            // this.setPrefWidth(width);
            // System.out.println(NodeUtil.getWidth(this));
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

    // public void cleaItem() {
    //     DestroyUtil.destroy(this.getItems());
    //     this.getItems().clear();
    // }

    // @Override
    // public void destroy() {
    //     this.getItems().removeListener(this.itemsListener);
    //     this.itemsListener = null;
    //     // DestroyUtil.destroy(this.getItems());
    //     this.getItems().clear();
    //     // this.cleaItem();
    //     // this.setStyle(null);
    //     // this.clearProps();
    // }

    @Override
    public void initNode() {
        // this.setAutoFix(false);
        // this.setMinWidth(160);
        this.sizeToScene();
        NodeAdapter.super.initNode();
    }

    @Override
    public void destroy() {
        this.getItems().removeListener(this.itemsListener);
        this.getItems().clear();
        NodeDestroyUtil.destroyObject(this);
    }
}
