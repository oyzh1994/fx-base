package cn.oyzh.fx.plus.controls.table;

import cn.oyzh.fx.plus.adapter.SelectAdapter;
import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.flex.FlexUtil;
import cn.oyzh.fx.plus.menu.ContextMenuAdapter;
import cn.oyzh.fx.plus.menu.MenuItemAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeGroup;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.node.NodeUtil;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import lombok.Getter;
import lombok.Setter;

/**
 * @author oyzh
 * @since 2022/1/18
 */
public class FXTableView<S> extends TableView<S> implements ContextMenuAdapter, MenuItemAdapter, FlexAdapter, NodeGroup, NodeAdapter, ThemeAdapter, SelectAdapter<S> {

    {
        NodeManager.init(this);
    }

    public FXTableView() {
    }

    protected void initTableView() {
        this.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        this.initEvenListener();
    }

    /**
     * 初始化事件监听器
     */
    protected void initEvenListener() {
//        this.focusedProperty().addListener((observableValue, aBoolean, t1) -> {
//            if(!t1){
//                this.clearSelection();
//            }
//        });
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
        this.initTableView();
//        this.setFocusTraversable(false);
//        this.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
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

    @Override
    public void resize(double width, double height) {
        double[] size = this.computeSize(width, height);
        super.resize(size[0], size[1]);
        this.resizeNode();
    }

    @Override
    public void resizeNode(Double width, Double height) {
        // 调用父类的resizeNode方法来调整节点大小
        FlexAdapter.super.resizeNode(width, height);

        // 获取表格中的列
        ObservableList<? extends TableColumn<?, ?>> columns = this.getColumns();
        // 遍历每一列
        for (TableColumn<?, ?> column : columns) {
            // 判断列是否是FlexAdapter的实例
            if (column instanceof FlexAdapter flexNode) {
                // 如果列可见，则设置实际宽度为计算得到的弹性宽度
                if (column.isVisible()) {
                    flexNode.setRealWidth(FlexUtil.compute(flexNode.getFlexWidth(), width));
                } else {
                    // 否则将列宽度设置为0
                    NodeUtil.setWidth(column, 0D);
                }
            }
        }
    }
}
