package cn.oyzh.fx.plus.controls.table;

import cn.oyzh.common.object.Destroyable;
import cn.oyzh.fx.plus.adapter.SelectAdapter;
import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.flex.FlexUtil;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.font.FontUtil;
import cn.oyzh.fx.plus.menu.ContextMenuAdapter;
import cn.oyzh.fx.plus.menu.MenuItemAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeDestroyUtil;
import cn.oyzh.fx.plus.node.NodeGroup;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.NestedTableColumnHeader;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

/**
 * 表格视图控件
 *
 * @author oyzh
 * @since 2022-01-18
 */
public class FXTableView<S> extends TableView<S> implements ContextMenuAdapter, MenuItemAdapter, FlexAdapter, NodeGroup, NodeAdapter, ThemeAdapter, SelectAdapter<S>, Destroyable, FontAdapter {

    {
        NodeManager.init(this);
    }

    /**
     * 初始化事件监听器
     */
    protected void initEvenListener() {
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
//        this.setCache(false);
        //        this.setHeaderHeight(30);
        //        this.setFixedCellSize(30);
        //        this.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        //        this.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        //        this.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        //        this.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_LAST_COLUMN);
        //        this.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_NEXT_COLUMN);
        //        this.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_SUBSEQUENT_COLUMNS);
        this.initEvenListener();
//        this.setReorderable(false);
        // 监听列
//        this.getColumns().addListener((ListChangeListener<TableColumn<S, ?>>) c -> {
//            if (c.next()) {
//                c.getAddedSubList().forEach(c1 -> c1.setReorderable(this.isReorderable()));
//            }
//        });
        FlexAdapter.super.initNode();
    }

    /**
     * 是否可重排列
     */
    private boolean reorderable;

    /**
     * 设置是否可重排列
     *
     * @param reorderable 是否可重排列
     */
    public void setReorderable(boolean reorderable) {
        this.reorderable = reorderable;
    }

    /**
     * 是否可重排列
     *
     * @return 结果
     */
    public boolean isReorderable() {
        return reorderable;
    }

    /**
     * Ctrl+S 动作
     */
    private Runnable ctrlSAction;

    /**
     * 获取 Ctrl+S 动作
     *
     * @return Ctrl+S 动作
     */
    public Runnable getCtrlSAction() {
        return ctrlSAction;
    }

    /**
     * 设置 Ctrl+S 动作
     *
     * @param ctrlSAction Ctrl+S 动作
     */
    public void setCtrlSAction(Runnable ctrlSAction) {
        this.ctrlSAction = ctrlSAction;
    }

    /**
     * 触发 Ctrl+S 动作
     */
    public void onCtrl_S() {
        if (this.ctrlSAction != null) {
            this.ctrlSAction.run();
        }
    }

    /**
     * 获取数据列表
     *
     * @return 数据列表
     */
    public ObservableList<S> itemList() {
        return this.itemsProperty().get();
    }

    /**
     * 设置双击单元格是否复制数据
     *
     * @param copyCellDataOnDoubleClicked 是否复制数据
     */
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

    /**
     * 双击单元格是否复制数据
     *
     * @return 结果
     */
    public boolean isCopyCellDataOnDoubleClicked() {
        return this.hasProp("_copyCellDataOnDoubleClicked");
    }

    /**
     * 监听选中项变更
     *
     * @param listener 监听器
     */
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
        FlexAdapter.super.resizeNode(width, height);
        ObservableList<? extends TableColumn<?, ?>> columns = this.getColumns();
        for (TableColumn<?, ?> column : columns) {
            if (column instanceof FlexAdapter flexNode && column.isVisible()) {
                flexNode.setRealWidth(FlexUtil.compute(flexNode.getFlexWidth(), width));
            }
        }
    }

    /**
     * 获取表头组件
     *
     * @return 表头组件
     */
    public Pane getHeader() {
        return TableViewUtil.getHeader(this);
    }

    /**
     * 获取表头高
     *
     * @return 表头高
     */
    public double getHeaderHeight() {
        Pane header = this.getHeader();
        return header == null ? -1D : header.getHeight();
    }

    /**
     * 设置表头高
     *
     * @param height 高
     */
    public void setHeaderHeight(double height) {
        Pane header = this.getHeader();
        if (header != null) {
            NestedTableColumnHeader columnHeader = TableViewUtil.getHeaderColumn(this);
            if (columnHeader != null) {
                columnHeader.maxHeightProperty().bind(header.maxHeightProperty());
                columnHeader.minHeightProperty().bind(header.minHeightProperty());
                columnHeader.prefHeightProperty().bind(header.prefHeightProperty());
            }
            header.setMaxHeight(height);
            header.setPrefHeight(height);
        } else {
            FXUtil.runPulse(() -> setHeaderHeight(height));
        }
    }

    @Override
    public void refresh() {
        FXUtil.runLater(super::refresh);
    }

    /**
     * 添加列
     *
     * @param column 列
     */
    public <T extends TableColumn<S, ?>> void addColumn(T column) {
        FXUtil.runWait(() -> super.getColumns().add(column));
    }

    /**
     * 添加多列
     *
     * @param columns 列列表
     */
    public void addColumn(List<? extends TableColumn<S, ?>> columns) {
        FXUtil.runWait(() -> super.getColumns().addAll(columns));
    }

    /**
     * 设置列
     *
     * @param columns 列列表
     */
    public void setColumn(List<? extends TableColumn<S, ?>> columns) {
        FXUtil.runWait(() -> super.getColumns().setAll(columns));
    }

    /**
     * 清空列
     */
    public void clearColumn() {
        FXUtil.runWait(() -> super.getColumns().clear());
    }

    /**
     * 字体
     */
    private Font font;

    @Override
    public void setFont(Font font) {
        this.font = font;
    }

    @Override
    public Font getFont() {
        return font;
    }

    @Override
    public void setFontSize(double fontSize) {
        this.setFont(FontUtil.newFontBySize(this.getFont(), fontSize));
    }

    @Override
    public void setFontFamily(String fontFamily) {
        this.setFont(FontUtil.newFontByFamily(this.getFont(), fontFamily));
    }

    @Override
    public void setFontWeight(FontWeight fontWeight) {
        this.setFont(FontUtil.newFontByWeight(this.getFont(), fontWeight));
    }

    @Override
    public void changeFont(Font font) {
        FontAdapter.super.changeFont(font);
    }

    @Override
    public void changeTheme(ThemeStyle style) {
        ThemeAdapter.super.changeTheme(style);
        this.refresh();
    }

    /**
     * 销毁节点
     */
    protected void destroyItems() {
        for (S item : this.getItems()) {
            if (item instanceof Destroyable destroyable) {
                destroyable.destroy();
            }
        }
    }

    /**
     * 移除时销毁节点
     */
    protected void destroyItemsOnRemoved() {
        // 监听移除
        this.itemList().addListener((ListChangeListener<S>) change -> {
            if (change.next()) {
                List<?> list = change.getRemoved();
                for (Object object : list) {
                    if (object instanceof Destroyable destroyable) {
                        destroyable.destroy();
                    }
                }
            }
        });
    }

    /**
     * 销毁列
     */
    protected void destroyColumn() {
        for (TableColumn<?, ?> column : this.getColumns()) {
            if (column instanceof Destroyable destroyable) {
                destroyable.destroy();
            }
        }
    }

    /**
     * 仅显示图标
     */
    public void showGraphicOnly() {
        for (TableColumn<S, ?> column : this.getColumns()) {
            if (column instanceof FXTableColumn<S, ?> tableColumn) {
                tableColumn.showGraphicOnly();
            }
        }
    }

    /**
     * 仅显示图标，延迟处理
     */
    public void showGraphicOnlyLater() {
        FXUtil.runPulse(this::showGraphicOnly);
    }

    /**
     * 设置选区模式
     *
     * @param selectionMode 选区模式
     */
    public void setSelectionMode(SelectionMode selectionMode) {
        this.getSelectionModel().setSelectionMode(selectionMode);
    }

    @Override
    public void destroy() {
//        this.clearProps();
        this.destroyItems();
        // this.clearItems();
        this.destroyColumn();
        this.clearColumn();
        NodeDestroyUtil.destroyNode(this);
        NodeDestroyUtil.destroyObject(this);
    }
}
