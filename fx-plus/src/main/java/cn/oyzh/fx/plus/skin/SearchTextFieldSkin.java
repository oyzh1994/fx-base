package cn.oyzh.fx.plus.skin;

import cn.oyzh.fx.plus.controls.popup.SearchHistoryPopup;
import cn.oyzh.fx.plus.controls.svg.HistorySVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;
import cn.oyzh.fx.plus.theme.ThemeManager;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import lombok.Getter;

/**
 * 搜索文本输入框皮肤
 *
 * @author oyzh
 * @since 2023/10/9
 */
public class SearchTextFieldSkin extends ClearableTextFieldSkin {

    /**
     * 搜索历史按钮
     */
    protected final SVGGlyph historyButton;

    /**
     * 搜索历史弹窗
     */
    @Getter
    protected SearchHistoryPopup historyPopup;

    /**
     * 设置历史弹窗组件
     *
     * @param popup 弹窗组件
     */
    public void setHistoryPopup(SearchHistoryPopup popup) {
        this.historyPopup = popup;
        if (this.historyPopup != null) {
            this.historyPopup.setOnHistorySelected(this::onHistorySelected);
        }
    }

    /**
     * 显示历史弹窗组件
     */
    protected void showHistoryPopup() {
        if (this.historyPopup != null) {
            if (this.historyPopup.isShowing()) {
                this.closeHistoryPopup();
            } else {
                this.historyPopup.show(this.getSkinnable());
            }
        }
    }

    /**
     * 关闭历史弹窗组件
     */
    protected void closeHistoryPopup() {
        if (this.historyPopup != null && this.historyPopup.isShowing()) {
            this.historyPopup.hide();
        }
    }

    /**
     * 搜索事件
     *
     * @param text 文本
     */
    protected void onSearch(String text) {
        this.closeHistoryPopup();
    }

    /**
     * 搜索历史词汇选中事件
     *
     * @param text 文本
     */
    protected void onHistorySelected(String text) {
        this.setText(text);
        this.closeHistoryPopup();
    }

    public SearchTextFieldSkin(TextField textField) {
        super(textField);
        // 初始化历史按钮
        this.historyButton = new HistorySVGGlyph();
        this.historyButton.setTipText(I18nHelper.his());
//        this.historyButton.setColor(this.getButtonColor());
        this.historyButton.setEnableWaiting(false);
        this.historyButton.setFocusTraversable(false);
        this.historyButton.setPadding(new Insets(0));
        this.historyButton.setOnMousePrimaryClicked(e -> this.showHistoryPopup());
        this.historyButton.setOnMouseMoved(mouseEvent -> this.historyButton.setColor("#E36413"));
        this.historyButton.setOnMouseExited(mouseEvent -> this.historyButton.setColor(this.getButtonColor()));
        this.getChildren().add(this.historyButton);

        // 按键监听
        this.getSkinnable().addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                this.onSearch(this.getText());
            } else if (event.getCode() == KeyCode.UP) {
                if (this.historyPopup != null) {
                    String kw = this.getText();
                    String text = this.historyPopup.getPrevHistory(kw);
                    if (text != null) {
                        this.onHistorySelected(text);
                        event.consume();
                    }
                }
            } else if (event.getCode() == KeyCode.DOWN) {
                if (this.historyPopup != null) {
                    String kw = this.getText();
                    String text = this.historyPopup.getNextHistory(kw);
                    if (text != null) {
                        this.onHistorySelected(text);
                        event.consume();
                    }
                }
            }
            this.closeHistoryPopup();
        });
        // 鼠标监听
        this.getSkinnable().addEventHandler(MouseEvent.MOUSE_PRESSED, event -> this.closeHistoryPopup());
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
        // 组件大小
        double size = h * .8;
        // 计算组件大小
        double btnSize = this.snapSizeX(size);
        // 设置组件大小
        this.historyButton.setSize(size);
        // 获取边距
        Insets padding = this.getSkinnable().getPadding();
        // 计算左边距
        double paddingLeft = btnSize + 8;
        // 设置左边距
        if (padding.getLeft() != paddingLeft) {
            padding = new Insets(padding.getTop(), padding.getRight(), padding.getBottom(), paddingLeft);
            this.getSkinnable().setPadding(padding);
        }
        // 设置组件位置
        super.positionInArea(this.historyButton, 3, y * 0.9, w, h, btnSize, HPos.LEFT, VPos.CENTER);
    }
}
