package cn.oyzh.fx.plus.menu;

import cn.oyzh.common.object.Destroyable;
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
 * 自定义上下文菜单，支持销毁、节点、布局与主题适配
 *
 * @author oyzh
 * @since 2023-10-09
 */
public class FXContextMenu extends ContextMenu implements Destroyable, NodeAdapter, LayoutAdapter, ThemeAdapter {

//    private final ListChangeListener<MenuItem> itemsListener = c -> this.calcWidth();

    {
        NodeManager.init(this);
    }

    /**
     * 目标对象的弱引用
     */
    private WeakReference<Object> targetRef;

    /**
     * 构造上下文菜单
     */
    public FXContextMenu() {
        this(null);
    }

    /**
     * 构造上下文菜单
     *
     * @param target 目标对象
     */
    public FXContextMenu(Object target) {
        super();
        if (target != null) {
            this.targetRef = new WeakReference<>(target);
        }
    }

    /**
     * 设置目标对象
     *
     * @param target 目标对象
     */
    public void setTarget(Object target) {
        this.targetRef = new WeakReference<>(target);
    }

//    private double width;

//    /**
//     * 计算菜单宽度
//     */
//    protected void calcWidth() {
//        ObservableList<MenuItem> items = this.getItems();
//        if (CollectionUtil.isNotEmpty(items)) {
//            double width = 0.d;
//            for (MenuItem item : items) {
//                double w = FXMenuItem.getWidth(item);
//                if (w > width) {
//                    width = w;
//                }
//            }
//            // 设置宽度
//            this.setWidth(width);
//            this.width = width;
//        } else {
//            this.width = Double.NaN;
//        }
//    }

    /**
     * 添加菜单项
     *
     * @param item 菜单项
     */
    public void addItem(MenuItem item) {
        if (item != null) {
            this.getItems().add(item);
        }
    }

    /**
     * 设置菜单项
     *
     * @param item 菜单项
     */
    public void setItem(MenuItem item) {
        if (item != null) {
            this.getItems().setAll(item);
        }
    }

    /**
     * 设置菜单项
     *
     * @param items 菜单项数组
     */
    public void setItem(MenuItem... items) {
        if (items != null) {
            //            DestroyUtil.destroy(this.getItems());
            this.getItems().setAll(items);
        }
    }

    /**
     * 设置菜单项
     *
     * @param items 菜单项集合
     */
    public void setItem(Collection<? extends MenuItem> items) {
        if (items != null) {
            //            DestroyUtil.destroy(this.getItems());
            this.getItems().setAll(items);
        }
    }

    @Override
    public void initNode() {
        ObjectWatcherManager.watch(this);
        this.sizeToScene();
        this.setStyle("-fx-padding: 0 0 0 0;");
//        this.getItems().addListener(this.itemsListener);
        this.showingProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
//                this.calcWidth();
//            } else {
                this.destroy();
            }
        });
//        this.prefWidthProperty().addListener((observable, oldValue, newValue) -> {
//            if (!Double.isNaN(this.width) && newValue.doubleValue() != this.width) {
//                this.getScene().getWindow().setWidth(this.width);
//                this.setPrefWidth(this.width);
//            }
//        });
        NodeAdapter.super.initNode();
    }

    @Override
    public void destroy() {
        for (MenuItem item : this.getItems()) {
            if (item instanceof Destroyable menuItem) {
                menuItem.destroy();
            } else {
                item.setText(null);
                item.setGraphic(null);
                item.setOnAction(null);
            }
        }
        this.getItems().clear();
        if (this.targetRef != null) {
            synchronized (this) {
                ContextMenuManager.clearContextMenu(this.targetRef.get());
                this.targetRef.clear();
                this.targetRef = null;
            }
        }
    }
}
