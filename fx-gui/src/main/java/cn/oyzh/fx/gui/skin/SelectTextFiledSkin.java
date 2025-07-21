package cn.oyzh.fx.gui.skin;

import cn.oyzh.fx.gui.svg.glyph.SelectSVGGlyph;
import cn.oyzh.fx.plus.controls.list.FXListView;
import cn.oyzh.fx.plus.controls.pane.FXScrollPane;
import cn.oyzh.fx.plus.controls.popup.FXPopup;
import cn.oyzh.fx.plus.node.NodeUtil;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.util.ListViewUtil;
import cn.oyzh.i18n.I18nHelper;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.util.List;

/**
 * 展开文本输入框皮肤
 *
 * @author oyzh
 * @since 2024/07/12
 */
public class SelectTextFiledSkin<T> extends ActionTextFieldSkin {

    /**
     * 弹窗组件
     */
    protected FXPopup popup;

    /**
     * 行高
     */
    protected double lineHeight = 25;

    /**
     * 转换器
     */
    private StringConverter<T> converter;

    /**
     * 节点变更事件
     */
    protected ChangeListener<T> selectItemChanged;

    /**
     * 下标变更事件
     */
    protected ChangeListener<Number> selectIndexChanged;

    public StringConverter<T> getConverter() {
        return converter;
    }

    public void setConverter(StringConverter<T> converter) {
        this.converter = converter;
    }

    public double getLineHeight() {
        return lineHeight;
    }

    public void setLineHeight(double lineHeight) {
        this.lineHeight = lineHeight;
    }

    public ChangeListener<T> selectItemChanged() {
        return selectItemChanged;
    }

    public void selectItemChanged(ChangeListener<T> selectItemChanged) {
        this.selectItemChanged = selectItemChanged;
    }

    public ChangeListener<Number> selectIndexChanged() {
        return selectIndexChanged;
    }

    public void selectIndexChanged(ChangeListener<Number> selectIndexChanged) {
        this.selectIndexChanged = selectIndexChanged;
    }

    @Override
    protected void onButtonClicked(MouseEvent event) {
        if (this.popup == null) {
            this.initPopup();
        }
        if (this.popup.isShowing()) {
            this.hidePopup();
        } else {
            this.showPopup();
        }
    }

    /**
     * 隐藏弹窗
     */
    public void hidePopup() {
        if (this.popup != null && this.popup.isShowing()) {
            this.popup.hide();
        }
    }

    /**
     * 显示弹窗
     */
    public void showPopup() {
        if (this.popup != null) {
            int size = this.getItemSize();
            double height = size * this.lineHeight + 10;
            if (height > 300) {
                height = 300;
            }
            this.popup.setHeight(height);
            TextField textField = this.getSkinnable();
            this.popup.showFixed(textField, -2, 0);
        }
    }

    /**
     * 初始化弹窗
     */
    protected void initPopup() {
        this.popup = new FXPopup();
        this.popup.setOnHidden(this::onPopupHide);
        this.popup.setOnShowing(this::onPopupShowing);
        Color color = ThemeManager.currentBackgroundColor();
        this.popup.getScene().setFill(color);
        TextField textField = this.getSkinnable();
        FXListView<T> listView = new FXListView<>();
        listView.setRealWidth(NodeUtil.getWidth(textField));
        // 数据函数
        Runnable dataFunc = () -> {
            T item = listView.getSelectedItem();
            // 设置数据中标志位
            textField.getProperties().put("texting", true);
            if (item == null) {
                textField.clear();
            } else if (this.converter != null) {
                textField.setText(this.converter.toString(item));
            } else {
                textField.setText(item.toString());
            }
            // 清除数据中标志位
            textField.getProperties().remove("texting");
        };
        listView.selectedItemChanged((observable, oldValue, newValue) -> {
            if (!listView.isIgnoreChanged()) {
                dataFunc.run();
                if (this.selectItemChanged != null) {
                    this.selectItemChanged.changed(observable, oldValue, newValue);
                }
                this.hidePopup();
            }
        });
        listView.selectedIndexChanged((observable, oldValue, newValue) -> {
            if (!listView.isIgnoreChanged()) {
                dataFunc.run();
                if (this.selectIndexChanged != null) {
                    this.selectIndexChanged.changed(observable, oldValue, newValue);
                }
                this.hidePopup();
            }
        });
        listView.setCellFactory(new Callback<>() {
            @Override
            public ListCell<T> call(ListView<T> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(T item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            this.setText(null);
                        } else {
                            if (converter != null) {
                                this.setText(converter.toString(item));
                            } else {
                                this.setText(item.toString());
                            }
                            this.setHeight(lineHeight);
                            this.setMaxHeight(lineHeight);
                            this.setMinHeight(lineHeight);
                            this.setPrefHeight(lineHeight);
                            ListViewUtil.highlightCell(this);
                        }
                    }
                };
            }
        });
        // 监听节点变化
        listView.getItems().addListener((ListChangeListener<T>) c -> listView.setRealHeight(listView.getItemSize() * this.lineHeight + 4));
        listView.setPadding(Insets.EMPTY);
        FXScrollPane scrollPane = new FXScrollPane(listView);
        scrollPane.setPadding(Insets.EMPTY);
        // 绑定大小
        listView.prefWidthProperty().bind(scrollPane.widthProperty());
        listView.prefHeightProperty().bind(scrollPane.heightProperty());
        scrollPane.prefWidthProperty().bind(textField.widthProperty());
        // scrollPane.setMaxHeight(150);
        // 同步布局
        if (NodeUtil.isOrientationRightToLeft(textField)) {
            scrollPane.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        }
        this.popup.content(scrollPane);
    }

    public SelectTextFiledSkin(TextField textField) {
        super(textField, new SelectSVGGlyph("12,10"));
        this.button.disappear();
        this.button.setTipText(I18nHelper.select());
    }

    @Override
    protected void setButtonSize(double size) {
        super.button.setSize(size * 1.5, size);
    }

    @Override
    protected void updateButtonVisibility() {
        boolean visible = this.getSkinnable().isVisible();
        boolean disable = this.getSkinnable().isDisable();
        boolean hasFocus = this.getSkinnable().isFocused();
        boolean shouldBeVisible = !disable && visible && hasFocus;
        this.button.setVisible(shouldBeVisible);
    }

    /**
     * 获取list列表组件
     *
     * @return list列表组件
     */
    protected FXListView<T> getListView() {
        FXScrollPane scrollPane = this.getScrollPane();
        return (FXListView<T>) scrollPane.getContent();
    }

    /**
     * 获取滚动组件
     *
     * @return 滚动组件
     */
    protected FXScrollPane getScrollPane() {
        if (this.popup == null) {
            this.initPopup();
        }
        return (FXScrollPane) this.popup.content();
    }

    /**
     * 计算大小
     */
    protected void calcSize(){
        this.getScrollPane().setRealHeight(this.getItemSize() * this.lineHeight + 4);
    }

    /**
     * 选择内容
     *
     * @param item 内容
     */
    public void selectItem(T item) {
        if (item != null && this.popup != null) {
            this.getListView().select(item);
        }
    }

    /**
     * 选择下标
     *
     * @param index 下标
     */
    public void selectIndex(int index) {
        this.getListView().select(index);
    }

    /**
     * 获取内容列表
     *
     * @return 内容列表
     */
    public List<T> getItemList() {
        return this.getListView().getItems();
    }

    /**
     * 设置内容列表
     *
     * @param itemList 内容列表
     */
    public void setItemList(List<T> itemList) {
        if (itemList == null) {
            return;
        }
        FXListView<T> listView = this.getListView();
        listView.setIgnoreChanged(true);
        listView.setItem(itemList);
        listView.setIgnoreChanged(false);
        this.calcSize();
    }

    /**
     * 获取内容大小
     *
     * @return 内容大小
     */
    public int getItemSize() {
        return this.getListView().getItemSize();
    }

    @Override
    protected double getButtonSizeMax() {
        return 10;
    }

    /**
     * 弹窗隐藏事件
     *
     * @param event 事件
     */
    protected void onPopupHide(WindowEvent event) {

    }

    /**
     * 弹窗显示中事件
     *
     * @param event 事件
     */
    protected void onPopupShowing(WindowEvent event) {

    }

    /**
     * 获取选中的节点
     *
     * @return 节点
     */
    public T getSelectedItem() {
        return this.getListView().getSelectedItem();
    }

    /**
     * 移除选区
     */
    public void clearSelection() {
        this.getListView().clearSelection();
    }

    /**
     * 获取选中的索引
     *
     * @return 索引
     */
    public int getSelectedIndex() {
        return this.getListView().getSelectedIndex();
    }

    /**
     * 是否输入文本中
     *
     * @return 结果
     */
    public boolean isTexting() {
        return this.getSkinnable().getProperties().containsKey("texting");
    }

    /**
     * 设置输入文本中
     */
    public void setTexting() {
        this.getSkinnable().getProperties().put("texting", true);
    }

    /**
     * 清除设置文本中
     */
    public void clearTexting() {
        this.getSkinnable().getProperties().remove("texting");
    }

}
