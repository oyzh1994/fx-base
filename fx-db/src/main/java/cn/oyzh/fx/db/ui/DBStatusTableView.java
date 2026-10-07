package cn.oyzh.fx.db.ui;

import cn.oyzh.fx.db.DBObject;
import cn.oyzh.fx.db.listener.DBStatusListener;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import javafx.collections.ListChangeListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 带状态管理的表格视图，负责维护表格数据项的状态监听及已删除数据项
 *
 * @author oyzh
 * @since 2024-07-22
 */
public class DBStatusTableView<S extends DBObject> extends FXTableView<S> {

    /**
     * 已删除的数据项
     */
    private List<S> deleteItems;

    /**
     * 重置，清空已删除项并清除所有数据项状态
     *
     * @throws Exception 异常
     */
    public void reset() throws Exception {
        this.deleteItems = null;
        this.clearStatus();
    }

    /**
     * 清除所有数据项的状态
     *
     * @throws Exception 异常
     */
    public void clearStatus() throws Exception {
        for (DBObject object : this.getItems()) {
            object.clearStatus();
        }
    }

    /**
     * 状态监听器
     */
    private DBStatusListener statusListener;

    // 监听数据项变化，为新加入项绑定状态监听，为移除项解绑并记录删除项
    {
        this.itemList().addListener((ListChangeListener<S>) c -> {
            if (this.statusListener == null) {
                return;
            }
            if (!c.next()) {
                return;
            }
            if (c.wasReplaced()) {
                List<? extends S> list = c.getList();
                if (list != null) {
                    for (S status : list) {
                        status.statusProperty().addListener(this.statusListener);
                    }
                }
            } else if (c.wasAdded()) {
                List<? extends S> list = c.getAddedSubList();
                if (list != null) {
                    for (DBObject status : list) {
                        status.statusProperty().addListener(this.statusListener);
                    }
                }
                this.statusListener.changed(null, null, null);
            } else if (c.wasRemoved()) {
                List<? extends S> list = c.getRemoved();
                if (list != null) {
                    for (S status : list) {
                        if (!status.isCreated()) {
                            if (this.deleteItems == null) {
                                this.deleteItems = new CopyOnWriteArrayList<>();
                            }
                            this.deleteItems.add(status);
                        }
                        status.statusProperty().removeListener(this.statusListener);
                    }
                }
            }
        });
    }

    /**
     * 获取删除项集合。
     *
     * @return 删除项集合
     */
    public List<S> getDeleteItems() {
        return deleteItems;
    }

    /**
     * 设置删除项集合。
     *
     * @param deleteItems 删除项集合
     */
    public void setDeleteItems(List<S> deleteItems) {
        this.deleteItems = deleteItems;
    }

    /**
     * 获取状态监听器。
     *
     * @return 状态监听器
     */
    public DBStatusListener getStatusListener() {
        return statusListener;
    }

    /**
     * 设置状态监听器。
     *
     * @param statusListener 状态监听器
     */
    public void setStatusListener(DBStatusListener statusListener) {
        this.statusListener = statusListener;
    }
}
