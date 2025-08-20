package cn.oyzh.fx.gui.skin;

import cn.oyzh.fx.gui.svg.glyph.SelectSVGGlyph;
import cn.oyzh.fx.plus.controls.list.FXListView;
import cn.oyzh.fx.plus.controls.pane.FXScrollPane;
import cn.oyzh.fx.plus.controls.popup.FXPopup;
import cn.oyzh.fx.plus.node.NodeUtil;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.util.ListViewUtil;
import cn.oyzh.i18n.I18nHelper;
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
import java.util.function.Consumer;

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
    protected Consumer<T> selectItemChanged;

    // /**
    //  * 下标变更事件
    //  */
    // protected Consumer<Integer> selectIndexChanged;

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

    public Consumer<T> selectItemChanged() {
        return selectItemChanged;
    }

    public void selectItemChanged(Consumer<T> selectItemChanged) {
        this.selectItemChanged = selectItemChanged;
    }

    // public ChangeListener<Number> selectIndexChanged() {
    //     return selectIndexChanged;
    // }
    //
    // public void selectIndexChanged(ChangeListener<Number> selectIndexChanged) {
    //     this.selectIndexChanged = selectIndexChanged;
    // }

    @Override
    protected void onButtonClicked(MouseEvent event) {
        if (this.popup == null) {
            this.initPopup();
        }
        if (this.popup.isShowing() || this.isItemEmpty()) {
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
        if (this.isItemEmpty()) {
            this.hidePopup();
            return;
        }
        if (this.popup != null) {
            this.calcSize();
            TextField textField = this.getSkinnable();
            this.popup.showFixed(textField, -1, 0);
        }
    }

    /**
     * 初始化弹窗
     */
    protected void initPopup() {
        this.popup = new FXPopup();
        this.popup.setAutoFix(true);
        this.popup.setOnHidden(this::onPopupHide);
        this.popup.setOnShowing(this::onPopupShowing);
        Color color = ThemeManager.currentBackgroundColor();
        this.popup.getScene().setFill(color);
        TextField textField = this.getSkinnable();
        FXListView<T> listView = new FXListView<>();
        // 数据函数
        Runnable dataFunc = () -> {
            T item = listView.getSelectedItem();
            // 设置数据中标志位
            this.setTexting();
            if (item == null) {
                textField.clear();
            } else if (this.converter != null) {
                textField.setText(this.converter.toString(item));
            } else {
                textField.setText(item.toString());
            }
            if (this.selectItemChanged != null) {
                this.selectItemChanged.accept(item);
            }
            this.hidePopup();
        };
        // listView.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
        //     if (!listView.isIgnoreChanged() && event.getCode() == KeyCode.ENTER) {
        //         dataFunc.run();
        //     }
        // });
        // listView.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
        //     if (!listView.isIgnoreChanged() && MouseUtil.isPrimaryButton(event)) {
        //         dataFunc.run();
        //     }
        // });
        listView.selectedItemChanged((observableValue, t, t1) -> {
            if (!listView.isIgnoreChanged()) {
                dataFunc.run();
            }
        });
        // listView.selectedItemChanged((observable, oldValue, newValue) -> {
        //     if (!listView.isIgnoreChanged()) {
        //         dataFunc.run();
        //         if (this.selectItemChanged != null) {
        //             this.selectItemChanged.changed(observable, oldValue, newValue);
        //         }
        //         this.hidePopup();
        //     }
        // });
        // listView.selectedIndexChanged((observable, oldValue, newValue) -> {
        //     if (!listView.isIgnoreChanged()) {
        //         dataFunc.run();
        //         if (this.selectIndexChanged != null) {
        //             this.selectIndexChanged.changed(observable, oldValue, newValue);
        //         }
        //         this.hidePopup();
        //     }
        // });
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
        // listView.getItems().addListener((ListChangeListener<T>) c -> listView.setRealHeight(listView.getItemSize() * this.lineHeight + 4));
        listView.setPadding(Insets.EMPTY);
        FXScrollPane scrollPane = new FXScrollPane(listView);
        scrollPane.setPadding(Insets.EMPTY);
        // 绑定大小
        scrollPane.prefWidthProperty().bind(textField.widthProperty());
        listView.prefWidthProperty().bind(textField.widthProperty());
        listView.prefHeightProperty().bind(scrollPane.heightProperty());
        // 同步布局
        if (NodeUtil.isOrientationRightToLeft(textField)) {
            scrollPane.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        }
        this.popup.content(scrollPane);
    }

    public SelectTextFiledSkin(TextField textField) {
        super(textField, new SelectSVGGlyph());
        this.button.disappear();
        this.button.setTipText(I18nHelper.select());
    }

    @Override
    protected void setButtonSize(double size) {
        super.button.setSize(size * 0.6 * 1.5, size * .6);
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
    public FXListView<T> listView() {
        FXScrollPane scrollPane = this.scrollPane();
        return (FXListView<T>) scrollPane.getContent();
    }

    /**
     * 获取滚动组件
     *
     * @return 滚动组件
     */
    protected FXScrollPane scrollPane() {
        if (this.popup == null) {
            this.initPopup();
        }
        return (FXScrollPane) this.popup.content();
    }

    /**
     * 计算大小
     */
    protected void calcSize() {
        // TextField textField = this.getSkinnable();
        FXScrollPane scrollPane = this.scrollPane();
        double height = this.getItemSize() * this.lineHeight + 4;
        // FXListView<T> listView = this.listView();
        if (height > 300) {
            height = 300;
            // double vWidth = ControlUtil.getVBarWidth(scrollPane);
            // double width = NodeUtil.getWidth(scrollPane);
            // width = width - Math.max(8, vWidth);
            // listView.setRealWidth(width);
            // } else {
            //     listView.setRealWidth(scrollPane.getRealWidth());
        }
        scrollPane.setRealHeight(height);
    }

    /**
     * 选择内容
     *
     * @param item 内容
     */
    public void selectItem(T item) {
        if (item != null && this.popup != null) {
            this.listView().select(item);
        }
    }

    /**
     * 选择首个
     */
    public void selectFirst() {
        this.listView().selectFirst();
    }

    /**
     * 选择下标
     *
     * @param index 下标
     */
    public void selectIndex(int index) {
        this.listView().select(index);
    }

    /**
     * 获取内容列表
     *
     * @return 内容列表
     */
    public List<T> getItemList() {
        return this.listView().getItems();
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
        FXListView<T> listView = this.listView();
        listView.setIgnoreChanged(true);
        listView.setItem(itemList);
        listView.setIgnoreChanged(false);
    }

    /**
     * 获取内容大小
     *
     * @return 内容大小
     */
    public int getItemSize() {
        return this.listView().getItemSize();
    }

    /**
     * 内容是否为空
     *
     * @return 结果
     */
    public boolean isItemEmpty() {
        return this.listView().isItemEmpty();
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
        return this.listView().getSelectedItem();
    }

    /**
     * 移除选区
     */
    public void clearSelection() {
        this.listView().clearSelection();
    }

    /**
     * 获取选中的索引
     *
     * @return 索引
     */
    public int getSelectedIndex() {
        return this.listView().getSelectedIndex();
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

    @Override
    protected double getButtonMargin() {
        return -3;
    }
}
