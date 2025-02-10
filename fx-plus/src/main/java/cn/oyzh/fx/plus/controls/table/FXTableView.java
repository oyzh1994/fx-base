package cn.oyzh.fx.plus.controls.table;

import cn.oyzh.fx.plus.adapter.SelectAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeGroup;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.util.TableViewUtil;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import lombok.Getter;
import lombok.Setter;

/**
 * @author oyzh
 * @since 2022/1/18
 */
public class FXTableView<S> extends TableView<S> implements NodeGroup, NodeAdapter, ThemeAdapter, SelectAdapter<S> {

    {
        NodeManager.init(this);
    }

    public FXTableView() {
    }

    /**
     * 获取当前选中列的数据
     *
     * @return 数据
     */
    public Object getSelectCellData() {
        return TableViewUtil.getSelectCellData(this);
    }

    @Override
    public void initNode() {
        this.setFixedCellSize(35.f);
//        this.setFocusTraversable(false);
        this.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
    }

    @Getter
    @Setter
    private Runnable ctrlSAction;

    public void onCtrl_S() {
        if (this.ctrlSAction != null) {
            this.ctrlSAction.run();
        }
    }

    public ObservableList<S> itemList() {
        return this.itemsProperty().get();
    }

    public void setCopyCellDataOnDoubleClicked(boolean copyCellDataOnDoubleClicked) {
        if (copyCellDataOnDoubleClicked) {
            EventHandler<MouseEvent> handler = TableViewUtil.copyCellDataOnDoubleClicked(this);
            this.setProp("_copyCellDataOnDoubleClicked", handler);
        } else {
            EventHandler<MouseEvent> handler = this.getProp("_copyCellDataOnDoubleClicked");
            if (handler != null) {
                this.removeEventHandler(MouseEvent.MOUSE_CLICKED, handler);
            }
        }
    }

    public boolean isCopyCellDataOnDoubleClicked() {
        return this.hasProp("_copyCellDataOnDoubleClicked");
    }

    public void selectedItemChanged(ChangeListener<S> listener) {
        this.getSelectionModel().selectedItemProperty().addListener(listener);
//        this.getSelectionModel().selectedItemProperty().addListener(new WeakChangeListener<>(listener));
    }
}
