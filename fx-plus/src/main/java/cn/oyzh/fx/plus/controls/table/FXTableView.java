package cn.oyzh.fx.plus.controls.table;

import cn.oyzh.fx.plus.adapter.SelectAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.util.TableViewUtil;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import lombok.Getter;
import lombok.Setter;

/**
 * @author oyzh
 * @since 2022/1/18
 */
public class FXTableView<S> extends TableView<S> implements NodeAdapter, ThemeAdapter, SelectAdapter<S> {

    {
        NodeManager.init(this);
    }

    private Callback<TableColumn, TableCell> defCellFactory;

    public FXTableView() {
        // this.initCellFactory();
        // // 监听列
        // this.getColumns().addListener((ListChangeListener<TableColumn<S, ?>>) c -> {
        //     // 设置cell工厂
        //     while (this.defCellFactory != null && c.next()) {
        //         List<? extends TableColumn> columns = null;
        //         if (c.wasAdded()) {
        //             columns = c.getAddedSubList();
        //         } else if (c.wasReplaced()) {
        //             columns = c.getList();
        //         }
        //         if (columns != null) {
        //             for (TableColumn column : columns) {
        //                 column.setCellFactory(this.defCellFactory);
        //                 column.getStyleClass().add("table-column-center-left");
        //             }
        //         }
        //     }
        // });
    }

    // protected void initCellFactory() {
    //     Pos cellPos = this.getCellPos();
    //     double cellHeight = this.getCellHeight();
    //     // cell工厂
    //     this.defCellFactory = param -> TableViewUtil.newCell(cellHeight, cellPos);
    //     // 设置cell工厂
    //     for (TableColumn column : this.getColumns()) {
    //         column.setCellFactory(this.defCellFactory);
    //         // column.setStyle("-fx-alignment: CENTER_LEFT;");
    //     }
    //
    //     this.addClass("table-view-left");
    // }
    //
    // public void setCellHeight(double cellHeight) {
    //     this.setProp("_cellHeight", cellHeight);
    //     this.initCellFactory();
    // }
    //
    // public double getCellHeight() {
    //     Double cellHeight = this.getProp("_cellHeight");
    //     return cellHeight == null ? 18 : cellHeight;
    // }
    //
    // public void setCellPos(Pos pos) {
    //     this.setProp("_cellPos", pos);
    //     this.initCellFactory();
    // }
    //
    // public Pos getCellPos() {
    //     Pos cellPos = this.getProp("_cellPos");
    //     return cellPos == null ? Pos.CENTER_LEFT : cellPos;
    // }
    //
    // public void setHeadPos(Pos pos) {
    //     this.setProp("_headPos", pos);
    // }
    //
    // public Pos getHeadPos() {
    //     Pos headPos = this.getProp("_headPos");
    //     return headPos == null ? Pos.CENTER_LEFT : headPos;
    // }

    /**
     * 获取当前选中列的数据
     *
     * @return 数据
     */
    public Object getSelectCellData() {
        return TableViewUtil.getSelectCellData(this);
    }

    @Override
    public void setInitIndex(int initIndex) {
        SelectAdapter.super.setInitIndex(initIndex);
    }

    @Override
    public int getInitIndex() {
        return SelectAdapter.super.getInitIndex();
    }

    @Override
    public void initNode() {
        this.setFixedCellSize(35.f);
        this.setFocusTraversable(false);
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
}
