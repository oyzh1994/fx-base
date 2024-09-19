package cn.oyzh.fx.plus.controls.popup;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.common.thread.ExecutorUtil;
import cn.oyzh.fx.plus.controls.view.FXListView;
import cn.oyzh.fx.plus.font.FontUtil;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.util.ControlUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.util.ListViewUtil;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
import javafx.util.Callback;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * 搜索历史弹窗
 *
 * @author oyzh
 * @since 2023/10/23
 */
public class SearchHistoryPopup extends FXPopup {

    {
        this.showingProperty().addListener((observableValue, windowEventEventHandler, t1) -> {
            if (BooleanUtil.isTrue(t1)) {
                this.initContent();
                this.calcListViewSize();
            } else {
                ExecutorUtil.start(this::clearHistories, 100);
            }
        });
        NodeManager.init(this);
    }

    /**
     * 选中事件
     */
    @Getter
    @Setter
    protected Consumer<String> onHistorySelected;

    /**
     * 单列数据高
     */
    @Getter
    @Setter
    protected double cellDataHeight = 20;

    /**
     * 初始化内容
     */
    protected void initContent() {
        FXListView<String> listView = this.listView();
        if (listView == null) {
            listView = new FXListView<>();
            this.content(listView);
            listView.setFontSize(11);
            listView.setCursor(Cursor.HAND);
            listView.selectedItemChanged((observableValue, s, t1) -> {
                if (this.onHistorySelected != null && this.isShowing()) {
                    this.onHistorySelected.accept(t1);
                }
            });

            listView.setCellFactory(new Callback<>() {
                @Override
                public ListCell<String> call(ListView<String> param) {
                    return new ListCell<>() {
                        @Override
                        protected void updateItem(String item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                this.setText(null);
                            } else {
                                this.setText(item);
                                this.setPrefHeight(cellDataHeight);
                                ListViewUtil.highlightCell(this);
                            }
                        }
                    };
                }
            });

        }
        this.setHistories(this.getHistories());
    }

    /**
     * 获取列表组件
     *
     * @return 列表组件
     */
    public FXListView<String> listView() {
        return (FXListView<String>) CollUtil.getFirst(this.getContent());
    }

    /**
     * 获取搜索历史
     *
     * @return 搜索历史
     */
    public List<String> getHistories() {
        return Collections.emptyList();
    }

    /**
     * 设置搜索历史
     *
     * @param histories 搜索历史
     */
    public void setHistories(List<String> histories) {
        if (histories != null && this.listView() != null) {
            this.listView().getItems().setAll(histories);
        }
    }

    /**
     * 清除搜索历史
     */
    public void clearHistories() {
        if (this.listView() != null) {
            FXUtil.runWait(() -> this.listView().getItems().clear());
        }
    }

    /**
     * 获取上一个搜索历史
     *
     * @param currKW 当前词汇
     * @return 上一个搜索历史
     */
    public String getPrevHistory(String currKW) {
        List<String> list = this.getHistories();
        if (list == null || list.isEmpty()) {
            return null;
        }
        String kw;
        // 获取首个
        if (currKW == null) {
            kw = CollUtil.getFirst(list);
        } else {
            int index = list.indexOf(currKW) + 1;
            // 获取最后一个
            if (index >= list.size()) {
                kw = CollUtil.getLast(list);
            } else {// 获取目标索引数据
                kw = list.get(index);
            }
        }
        return kw;
    }

    /**
     * 获取下一个搜索历史
     *
     * @param currKW 当前词汇
     * @return 下一个搜索历史
     */
    public String getNextHistory(String currKW) {
        List<String> list = this.getHistories();
        if (list == null || list.isEmpty()) {
            return null;
        }
        String kw;
        // 获取最后一个
        if (StrUtil.isEmpty(currKW)) {
            kw = CollUtil.getLast(list);
        } else {
            int index = list.indexOf(currKW) - 1;
            // 获取首个
            if (index <= 0) {
                kw = CollUtil.getFirst(list);
            } else {// 获取目标索引数据
                kw = list.get(index);
            }
        }
        return kw;
    }

    /**
     * 计算列表组件大小
     */
    public void calcListViewSize() {
        FXListView<String> listView = this.listView();
        if (listView == null) {
            return;
        }
        List<String> list = this.getHistories();
        // 无数据设置默认宽高
        if (CollUtil.isEmpty(list)) {
            listView.setRealWidth(50);
            listView.setRealHeight(120);
        } else {
            // 计算列表视图宽
            double width = 0;
            Font font = this.listView().getFont();
            for (String s : list) {
                double w = FontUtil.stringWidth(s, font);
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
            double height = this.cellDataHeight * size + 3;
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
    public void show(@NonNull Node ownerNode, @NonNull MouseEvent event) {
        this.show(ownerNode, event.getScreenX(), event.getScreenY());
    }

    /**
     * 显示组件
     *
     * @param ownerNode 父节点
     */
    public void show(@NonNull Node ownerNode) {
        Point2D point2D = ownerNode.localToScreen(ownerNode.getScaleX(), ownerNode.getScaleY());
        double height = ControlUtil.boundedHeight(ownerNode);
        this.show(ownerNode, point2D.getX(), point2D.getY() + height);
    }
}
