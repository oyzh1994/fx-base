package cn.oyzh.fx.plus.controls.popup;

import cn.oyzh.common.thread.ExecutorUtil;
import cn.oyzh.common.util.BooleanUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.fx.plus.controls.list.FXListView;
import cn.oyzh.fx.plus.font.FontUtil;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.util.ListViewUtil;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
import javafx.util.Callback;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * 列表弹出框控件
 *
 * @author oyzh
 * @since 2023/12/22
 */
public class ListViewPopup<E> extends FXPopup {

    {

        NodeManager.init(this);
        this.initPopup();
    }

    /**
     * 元素选中事件
     */
    protected Consumer<E> onItemSelected;

    /**
     * 索引选中事件
     */
    protected Consumer<Integer> onIndexSelected;

    /**
     * 选中的项
     */
    private E selectedItem;

    /**
     * 选中的索引
     */
    private Integer selectedIndex;

    /**
     * 单列数据高
     */
    protected double cellLineHeight = 20;

    /**
     * 初始化弹窗
     */
    protected void initPopup() {
        this.showingProperty().addListener((observableValue, windowEventEventHandler, t1) -> {
            if (BooleanUtil.isTrue(t1)) {
                this.initContent();
                this.calcListViewSize();
            } else {
                ExecutorUtil.start(this::clearItems, 100);
            }
        });
    }

    /**
     * 初始化内容
     */
    protected void initContent() {
        FXListView<E> listView = this.listView();
        if (listView == null) {
            listView = new FXListView<>();
            this.initListView(listView);
            this.content(listView);
            listView.selectedItemChanged((observableValue, s, t1) -> {
                if (this.onItemSelected != null && this.isShowing()) {
                    this.selectedItem = t1;
                    this.onItemSelected.accept(t1);
                }
            });
            listView.selectedIndexChanged((observableValue, s, t1) -> {
                if (this.onIndexSelected != null && this.isShowing()) {
                    this.selectedIndex = t1.intValue();
                    this.onIndexSelected.accept(t1.intValue());
                }
            });
            listView.setCellFactory(new Callback<>() {
                @Override
                public ListCell<E> call(ListView<E> param) {
                    return new ListCell<>() {
                        @Override
                        protected void updateItem(E item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                this.setText(null);
                            } else {
                                if (selectedItem != null && Objects.equals(selectedItem, item)) {
                                    this.setText("✔ " + item);
                                } else if (selectedIndex != null && Objects.equals(getItems().get(selectedIndex), item)) {
                                    this.setText("✔ " + item);
                                } else {
                                    this.setText("    " + item);
                                }
                                this.setHeight(cellLineHeight);
                                this.setMinHeight(cellLineHeight);
                                this.setMaxHeight(cellLineHeight);
                                this.setPrefHeight(cellLineHeight);
                                ListViewUtil.highlightCell(this);
                            }
                        }
                    };
                }
            });
        }
        this.setItems(this.getItems());
    }

    /**
     * 初始化列表组件
     *
     * @param listView 列表组件
     */
    protected void initListView(FXListView<E> listView) {
        listView.setFontSize(11);
        listView.setCursor(Cursor.HAND);
    }

    /**
     * 获取列表组件
     *
     * @return 列表组件
     */
    public FXListView<E> listView() {
        return (FXListView<E>) CollectionUtil.getFirst(this.getContent());
    }

    /**
     * 获取列表数据
     *
     * @return 数据列表
     */
    public List<E> getItems() {
        return List.of();
    }

    /**
     * 设置列表数据
     *
     * @param items 数据列表
     */
    public void setItems(List<E> items) {
        if (items != null && this.listView() != null) {
            FXUtil.runWait(() -> this.listView().setItem(items));
        }
    }

    /**
     * 清除列表数据
     */
    public void clearItems() {
        if (this.listView() != null) {
            FXUtil.runWait(() -> this.listView().clearItems());
        }
    }

    /**
     * 计算列表组件大小
     */
    public void calcListViewSize() {
        FXListView<E> listView = this.listView();
        if (listView == null) {
            return;
        }
        List<E> list = this.getItems();
        // 无数据设置默认宽高
        if (CollectionUtil.isEmpty(list)) {
            listView.setRealWidth(50);
            listView.setRealHeight(120);
        } else {
            // 计算列表视图宽
            double width = 0;
            Font font = this.listView().getFont();
            for (E s : list) {
                double w = FontUtil.stringWidth(s.toString(), font);
                if (w > width) {
                    width = w;
                }
            }
            // 限制宽度
            if (width > 300) {
                width = 300;
            } else {
                width += 60;
            }
            // 显示个数限制为10个
            int size = Math.min(list.size(), 10);
            // 修正高
            double height = this.cellLineHeight * size + 3;
            listView.setRealWidth(width);
            listView.setRealHeight(height);
        }
    }

    /**
     * 显示组件
     *
     * @param ownerNode 父节点
     * @param event     鼠标事件
     */
    public void show(Node ownerNode, MouseEvent event) {
        this.show(ownerNode, event.getScreenX(), event.getScreenY());
    }

    /**
     * 获取项选中。
     *
     * @return 项选中
     */
    public Consumer<E> getOnItemSelected() {
        return onItemSelected;
    }

    /**
     * 设置项选中。
     *
     * @param onItemSelected 项选中回调
     */
    public void setOnItemSelected(Consumer<E> onItemSelected) {
        this.onItemSelected = onItemSelected;
    }

    /**
     * 获取索引选中。
     *
     * @return 索引选中
     */
    public Consumer<Integer> getOnIndexSelected() {
        return onIndexSelected;
    }

    /**
     * 设置索引选中。
     *
     * @param onIndexSelected 索引选中
     */
    public void setOnIndexSelected(Consumer<Integer> onIndexSelected) {
        this.onIndexSelected = onIndexSelected;
    }

    /**
     * 获取选中项。
     *
     * @return 选中项
     */
    public E getSelectedItem() {
        return selectedItem;
    }

    /**
     * 设置选中项。
     *
     * @param selectedItem 选中项
     */
    public void setSelectedItem(E selectedItem) {
        this.selectedItem = selectedItem;
    }

    /**
     * 获取选中索引。
     *
     * @return 选中索引
     */
    public Integer getSelectedIndex() {
        return selectedIndex;
    }

    /**
     * 设置选中索引。
     *
     * @param selectedIndex 选中索引
     */
    public void setSelectedIndex(Integer selectedIndex) {
        this.selectedIndex = selectedIndex;
    }

    /**
     * 获取单元格行高度。
     *
     * @return 单元格行高度
     */
    public double getCellLineHeight() {
        return cellLineHeight;
    }

    /**
     * 设置单元格行高度。
     *
     * @param cellLineHeight 单元格行高度
     */
    public void setCellLineHeight(double cellLineHeight) {
        this.cellLineHeight = cellLineHeight;
    }
}
