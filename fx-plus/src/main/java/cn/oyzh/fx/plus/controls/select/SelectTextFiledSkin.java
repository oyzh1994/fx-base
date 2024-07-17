package cn.oyzh.fx.plus.controls.select;

import cn.oyzh.fx.plus.controls.view.FlexListView;
import cn.oyzh.fx.plus.controls.popup.FXPopup;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SelectSVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;
import cn.oyzh.fx.plus.skin.TextFieldSkinExt;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.util.ListViewUtil;
import cn.oyzh.fx.plus.util.NodeUtil;
import javafx.beans.value.ChangeListener;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 展开文本输入框皮肤
 *
 * @author oyzh
 * @since 2024/07/12
 */
public class SelectTextFiledSkin extends TextFieldSkinExt {

    /**
     * 行高
     */
    @Getter
    @Setter
    protected double lineHeight = 25;

    /**
     * 选中的索引位
     */
    @Getter
    protected int selectedIndex = -1;

    /**
     * 数据列表
     */
    @Getter
    @Setter
    protected List<String> dataList;

    /**
     * 行高
     */
    @Getter
    @Setter
    protected ChangeListener<Number> selectIndexChanged;

    /**
     * 展开按钮
     */
    protected final SVGGlyph selectButton;

    /**
     * 展开组件
     */
    protected FXPopup popup;

    /**
     * 显示历史弹窗组件
     */
    protected void onSelectButtonClick() {
        if (this.popup == null) {
            this.popup = new FXPopup();
        }
        if (this.popup.isShowing()) {
            this.popup.hide();
        } else {
            if (this.dataList == null) {
                this.dataList = new ArrayList<>();
            }
            TextField textField = this.getSkinnable();
            FlexListView<String> listView = new FlexListView<>();
            listView.setItem(this.dataList);
            listView.setRealWidth(NodeUtil.getWidth(textField));
            listView.selectedItemChanged((observable, oldValue, newValue) -> {
                textField.setText(newValue);
                this.selectedIndex = listView.getSelectedIndex();
                this.popup.hide();
            });
            if (this.selectIndexChanged != null) {
                listView.selectedIndexChanged(this.selectIndexChanged);
            }
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
                                this.setPrefHeight(lineHeight);
                                ListViewUtil.highlightCell(this);
                            }
                        }
                    };
                }
            });
            listView.setRealHeight(this.dataList.size() * this.lineHeight + 2);
            textField.widthProperty().addListener((observable, oldValue, newValue) -> listView.setRealWidth(NodeUtil.getWidth(textField)));
            this.popup.setContent(listView);
            this.popup.showFixed(textField, -2, 0);
        }
    }

    public SelectTextFiledSkin(TextField textField) {
        super(textField);
        // 初始化历史按钮
        this.selectButton = new SelectSVGGlyph();
        this.selectButton.setSizeStr("13");
        this.selectButton.setTipText(I18nHelper.select());
        this.selectButton.setEnableWaiting(false);
        this.selectButton.setFocusTraversable(false);
        this.selectButton.setPadding(new Insets(0));
        this.selectButton.setOnMousePrimaryClicked(e -> this.onSelectButtonClick());
        this.selectButton.setOnMouseMoved(mouseEvent -> this.selectButton.setColor("#E36413"));
        this.selectButton.setOnMouseExited(mouseEvent -> this.selectButton.setColor(this.getButtonColor()));
        this.getChildren().add(this.selectButton);
    }

    @Override
    protected Color getButtonColor() {
        if (!ThemeManager.isDarkMode()) {
            return Color.valueOf("#696969");
        }
        return super.getButtonColor();
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        super.layoutChildren(x, y, w, h);
        double h1 = h * 0.5;
        double w1 = h1 * 0.7;
        // 按钮大小
        this.selectButton.setSizeStr(h1 + "," + w1);
        // 计算组件大小
        double btnSize = this.snapSizeX(w1);
        // 位移的areaX值，规则 组件宽+x-按钮大小
        double areaX = w + x - btnSize - 12;
        // 设置位置
        super.positionInArea(this.selectButton, areaX, y, btnSize, h, 0, HPos.CENTER, VPos.CENTER);
    }
}
