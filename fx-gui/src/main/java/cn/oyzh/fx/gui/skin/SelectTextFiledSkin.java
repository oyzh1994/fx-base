package cn.oyzh.fx.gui.skin;

import cn.oyzh.fx.gui.svg.glyph.SelectSVGGlyph;
import cn.oyzh.fx.plus.controls.list.FXListView;
import cn.oyzh.fx.plus.controls.popup.FXPopup;
import cn.oyzh.fx.plus.node.NodeUtil;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.util.ListViewUtil;
import cn.oyzh.i18n.I18nHelper;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import java.util.List;

/**
 * 展开文本输入框皮肤
 *
 * @author oyzh
 * @since 2024/07/12
 */
public class SelectTextFiledSkin extends ActionTextFieldSkin {

    /**
     * 行高
     */
    protected double lineHeight = 25;

    public double getLineHeight() {
        return lineHeight;
    }

    public void setLineHeight(double lineHeight) {
        this.lineHeight = lineHeight;
    }

//    /**
//     * 元素选中事件
//     */
//    protected ChangeListener<String> selectItemChanged;
//
//    public ChangeListener<String> getSelectItemChanged() {
//        return selectItemChanged;
//    }
//
//    public void setSelectItemChanged(ChangeListener<String> selectItemChanged) {
//        this.selectItemChanged = selectItemChanged;
//    }

    /**
     * 下标选中事件
     */
    protected ChangeListener<Number> selectIndexChanged;

    public ChangeListener<Number> getSelectIndexChanged() {
        return selectIndexChanged;
    }

    public void setSelectIndexChanged(ChangeListener<Number> selectIndexChanged) {
        this.selectIndexChanged = selectIndexChanged;
    }

    /**
     * 展开组件
     */
    protected FXPopup popup;

    @Override
    protected void onButtonClicked(MouseEvent event) {
        if (this.popup == null) {
            this.initPopup();
        }
        if (this.popup.isShowing()) {
            this.popup.hide();
        } else {
            TextField textField = this.getSkinnable();
            this.popup.showFixed(textField, -2, 0);
        }
    }

    protected void initPopup() {
        this.popup = new FXPopup();
        Color color = ThemeManager.currentBackgroundColor();
        this.popup.getScene().setFill(color);
        TextField textField = this.getSkinnable();
        FXListView<String> listView = new FXListView<>();
        listView.setRealWidth(NodeUtil.getWidth(textField));
//        listView.selectedItemChanged((observable, oldValue, newValue) -> {
//            if (this.selectItemChanged != null) {
//                this.selectItemChanged.changed(observable, oldValue, newValue);
//            }
//            textField.setText(newValue);
//            this.popup.hide();
//        });
        listView.selectedIndexChanged((observable, oldValue, newValue) -> {
            if (!listView.isIgnoreChanged()) {
                String text = listView.getSelectedItem();
                if (text == null) {
                    textField.clear();
                } else {
                    textField.setText(text);
                }
                if (this.selectIndexChanged != null) {
                    this.selectIndexChanged.changed(observable, oldValue, newValue);
                }
                this.popup.hide();
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
        listView.getItems().addListener((ListChangeListener<String>) c -> listView.setRealHeight(listView.getItemSize() * this.lineHeight + 4));
        textField.widthProperty().addListener((observable, oldValue, newValue) -> listView.setRealWidth(NodeUtil.getWidth(textField)));
        this.popup.content(listView);
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

    //    @Override
//    protected void layoutChildren(double x, double y, double w, double h) {
//        super.layoutChildren(x, y, w, h);
//        double h1 = h * 0.5;
//        double w1 = h1 * 0.7;
//        // 按钮大小
//        this.button.setSizeStr(h1 + "," + w1);
//        // 计算组件大小
//        double btnSize = this.snapSizeX(w1);
//        // 位移的areaX值，规则 组件宽+x-按钮大小
//        double areaX = w + x - btnSize - 12;
//        // 设置位置
//        super.positionInArea(this.button, areaX, y, btnSize, h, 0, HPos.CENTER, VPos.CENTER);
//    }

    @Override
    protected void updateButtonVisibility() {
        boolean visible = this.getSkinnable().isVisible();
        boolean disable = this.getSkinnable().isDisable();
        boolean hasFocus = this.getSkinnable().isFocused();
        boolean shouldBeVisible = !disable && visible && hasFocus;
        this.button.setVisible(shouldBeVisible);
    }

    protected FXListView<String> getListView() {
        if (this.popup == null) {
            this.initPopup();
        }
        return (FXListView<String>) this.popup.content();
    }

    public void selectItem(String item) {
        if (item != null && this.popup != null && this.popup.isShowing()) {
            this.getListView().select(item);
        }
    }

    public void selectIndex(int index) {
        this.getListView().select(index);
    }

    public List<String> getItemList() {
        return this.getListView().getItems();
    }

    public void setItemList(List<String> itemList) {
        if (itemList == null) {
            return;
        }
        FXListView<String> listView = this.getListView();
        listView.setIgnoreChanged(true);
        listView.setItem(itemList);
        listView.setRealHeight(listView.getItemSize() * this.lineHeight + 4);
        listView.setIgnoreChanged(false);
    }

    public int getItemSize() {
        return this.getListView().getItemSize();
    }

    @Override
    protected double getButtonSizeMax() {
        return 10;
    }
}
