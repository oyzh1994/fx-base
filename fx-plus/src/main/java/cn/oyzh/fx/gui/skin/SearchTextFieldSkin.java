package cn.oyzh.fx.gui.skin;

import cn.oyzh.fx.gui.svg.glyph.HistorySVGGlyph;
import cn.oyzh.fx.plus.controls.popup.SearchHistoryPopup;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.node.NodeDestroyUtil;
import cn.oyzh.i18n.I18nHelper;
import javafx.beans.property.ObjectProperty;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

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
    protected SVGGlyph history;

    /**
     * 搜索历史弹窗
     */
    protected SearchHistoryPopup popup;

    public SearchHistoryPopup getPopup() {
        return popup;
    }

    /**
     * 设置历史弹窗组件
     *
     * @param popup 弹窗组件
     */
    public void setPopup(SearchHistoryPopup popup) {
        this.popup = popup;
        if (this.popup != null) {
            this.popup.setOnHistorySelected(this::onHistorySelected);
        }
    }

    /**
     * 显示历史弹窗组件
     */
    protected void showPopup() {
        if (this.popup != null) {
            if (this.popup.isShowing()) {
                this.closePopup();
            } else {
                this.popup.show(this.getSkinnable());
            }
        }
    }

    /**
     * 关闭历史弹窗组件
     */
    protected void closePopup() {
        if (this.popup != null && this.popup.isShowing()) {
            this.popup.hide();
        }
    }

    /**
     * 搜索事件
     *
     * @param text 文本
     */
    protected void onSearch(String text) {
        this.closePopup();
    }

    /**
     * 搜索历史词汇选中事件
     *
     * @param text 文本
     */
    protected void onHistorySelected(String text) {
        this.setText(text);
        this.closePopup();
    }

    private EventHandler<? super KeyEvent> onKeyPressed = event -> {
        if (event.getCode() == KeyCode.ENTER) {
            this.onSearch(this.getText());
        } else if (event.getCode() == KeyCode.UP) {
            if (this.popup != null) {
                String kw = this.getText();
                String text = this.popup.getPrevHistory(kw);
                if (text != null) {
                    this.onHistorySelected(text);
                    event.consume();
                }
            }
        } else if (event.getCode() == KeyCode.DOWN) {
            if (this.popup != null) {
                String kw = this.getText();
                String text = this.popup.getNextHistory(kw);
                if (text != null) {
                    this.onHistorySelected(text);
                    event.consume();
                }
            }
        }
        this.closePopup();
    };

    private EventHandler<? super MouseEvent> onMousePressed = event -> {
        this.closePopup();
    };

    public SearchTextFieldSkin(TextField textField) {
        super(textField);
//         // 初始化历史按钮
//         this.historyButton = new HistorySVGGlyph();
//         this.historyButton.setTipText(I18nHelper.his());
// //        this.historyButton.setColor(this.getButtonColor());
//         this.historyButton.setEnableWaiting(false);
//         this.historyButton.setFocusTraversable(false);
// //        this.historyButton.setPadding(new Insets(0));
//         this.historyButton.setOnMousePrimaryClicked(e -> this.showHistoryPopup());
//         this.historyButton.setOnMouseMoved(mouseEvent -> this.historyButton.setColor("#E36413"));
//         this.historyButton.setOnMouseExited(mouseEvent -> this.historyButton.setColor(this.getButtonColor()));
//         this.getChildren().add(this.historyButton);
        // 按键监听
        this.getSkinnable().addEventFilter(KeyEvent.KEY_PRESSED, this.onKeyPressed);
        // 鼠标监听
        this.getSkinnable().addEventFilter(MouseEvent.MOUSE_PRESSED, this.onMousePressed);
    }

//    @Override
//    protected Color getButtonColor() {
//        if (!ThemeManager.isDarkMode()) {
//            return Color.valueOf("#696969");
//        }
//        return super.getButtonColor();
//    }

    // @Override
    // protected void layoutChildren(double x, double y, double w, double h) {
    //     super.layoutChildren(x, y, w, h);
    //     // 组件大小
    //     double size = h * .8;
    //     // 计算组件大小
    //     double btnSize = this.snapSizeX(size);
    //     // 设置组件大小
    //     this.historyButton.setSize(size);
    //     // 获取边距
    //     Insets padding = this.getSkinnable().getPadding();
    //     // 计算左边距
    //     double paddingLeft = btnSize + 8;
    //     // 设置左边距
    //     if (padding.getLeft() != paddingLeft) {
    //         padding = new Insets(padding.getTop(), padding.getRight(), padding.getBottom(), paddingLeft);
    //         this.getSkinnable().setPadding(padding);
    //     }
    //     // 设置组件位置
    //     // super.positionInArea(this.historyButton, 3, y * 0.9, w, h, btnSize, HPos.LEFT, VPos.CENTER);
    //     super.positionInArea(this.historyButton, 3, y * 0.9, 0, h, btnSize, HPos.LEFT, VPos.CENTER);
    // }

    @Override
    public ObjectProperty<Node> leftProperty() {
        if (super.leftProperty== null) {
            this.history = new HistorySVGGlyph();
            this.history.setTipText(I18nHelper.his());
            this.history.setFocusTraversable(false);
            this.history.setPadding(DEFAULT_LEFT_PADDING);
            this.history.setOnMousePrimaryClicked(e -> this.showPopup());
            this.history.setOnMouseMoved(mouseEvent -> this.history.setColor("#E36413"));
            this.history.setOnMouseExited(mouseEvent -> this.history.setColor(this.getButtonColor()));
            super.leftProperty().set(this.history);
        }
        return super.leftProperty();
    }

    @Override
    public void dispose() {
        NodeDestroyUtil.destroyObject(this.history);
        this.history = null;
        NodeDestroyUtil.destroyObject(this.popup);
        this.popup = null;
        this.getSkinnable().removeEventFilter(KeyEvent.KEY_PRESSED, this.onKeyPressed);
        this.getSkinnable().removeEventFilter(MouseEvent.MOUSE_PRESSED, this.onMousePressed);
        this.onKeyPressed = null;
        this.onMousePressed = null;
        super.dispose();
    }
}
