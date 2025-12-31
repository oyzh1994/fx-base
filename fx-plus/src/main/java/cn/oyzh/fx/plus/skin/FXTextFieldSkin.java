package cn.oyzh.fx.plus.skin;

import atlantafx.base.controls.CustomTextFieldSkin;
import cn.oyzh.fx.plus.information.TooltipExt;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Window;

/**
 * 文本域皮肤扩展
 *
 * @author oyzh
 * @since 2023/10/25
 */
public class FXTextFieldSkin extends CustomTextFieldSkin {

    /**
     * 大小监听器
     */
    protected InvalidationListener sizeChanged = observable -> this.onSizeChanged();

    /**
     * 可见监听器
     */
    protected InvalidationListener visibilityChanged = observable -> this.updateButtonVisibility();

    /**
     * 修改按钮显示状态
     */
    protected void updateButtonVisibility() {
    }

    public FXTextFieldSkin(TextField textField) {
        super(textField);
        // 初始化监听器
        // WeakInvalidationListener visibilityChangedListener = new WeakInvalidationListener(this.visibilityChanged);
        // control.textProperty().addListener(visibilityChangedListener);
        // control.focusedProperty().addListener(visibilityChangedListener);
        // control.visibleProperty().addListener(visibilityChangedListener);
        // control.disableProperty().addListener(visibilityChangedListener);
        textField.textProperty().addListener(this.visibilityChanged);
        textField.focusedProperty().addListener(this.visibilityChanged);
        textField.visibleProperty().addListener(this.visibilityChanged);
        textField.disableProperty().addListener(this.visibilityChanged);
        // WeakInvalidationListener sizeChangedListener = new WeakInvalidationListener(this.sizeChanged);
        textField.widthProperty().addListener(this.sizeChanged);
        textField.heightProperty().addListener(this.sizeChanged);
        // 更新一次按钮显示状态
        this.updateButtonVisibility();
    }

    /**
     * 左边组件属性
     */
    protected SimpleObjectProperty<Node> leftProperty;

    @Override
    public ObjectProperty<Node> leftProperty() {
        if (this.leftProperty == null) {
            this.leftProperty = new SimpleObjectProperty<>();
        }
        return this.leftProperty;
    }

    /**
     * 获取左边组件
     *
     * @return 组件
     */
    public Node getLeft() {
        return this.leftProperty == null ? null : this.leftProperty.get();
    }

    /**
     * 右边组件属性
     */
    protected SimpleObjectProperty<Node> rightProperty;

    @Override
    public ObjectProperty<Node> rightProperty() {
        if (this.rightProperty == null) {
            this.rightProperty = new SimpleObjectProperty<>();
        }
        return this.rightProperty;
    }

    /**
     * 获取右边组件
     *
     * @return 组件
     */
    public Node getRight() {
        return this.rightProperty == null ? null : this.rightProperty.get();
    }

    @Override
    public void dispose() {
        // 清除监听器
        this.getSkinnable().widthProperty().removeListener(this.sizeChanged);
        this.getSkinnable().heightProperty().removeListener(this.sizeChanged);
        this.sizeChanged = null;
        this.getSkinnable().textProperty().removeListener(this.visibilityChanged);
        this.getSkinnable().focusedProperty().removeListener(this.visibilityChanged);
        this.getSkinnable().visibleProperty().removeListener(this.visibilityChanged);
        this.getSkinnable().disableProperty().removeListener(this.visibilityChanged);
        this.visibilityChanged = null;
        this.getSkinnable().setOnMouseExited(null);
        this.getSkinnable().setOnMouseEntered(null);
        if (this.leftProperty != null) {
            this.leftProperty.unbind();
            this.leftProperty = null;
        }
        if (this.rightProperty != null) {
            this.rightProperty.unbind();
            this.rightProperty = null;
        }
        FXUtil.runLater(super::dispose);
    }

    /**
     * 获取按钮颜色
     *
     * @return 按钮颜色
     */
    protected Color getButtonColor() {
        // if (ThemeManager.isDarkMode()) {
        //     return Color.WHITE;
        // }
        // return Color.BLACK;
        return ThemeManager.currentAccentColor();
    }

    /**
     * 获取文本
     *
     * @return 文本
     */
    protected String getText() {
        return this.getSkinnable().getText();
    }

    /**
     * 设置文本
     *
     * @param text 文本
     */
    protected void setText(String text) {
        this.getSkinnable().setText(text);
    }

    /**
     * 设置提示文本
     *
     * @param tipText 提示文本
     */
    protected void setTipText(String tipText) {
        TooltipExt tooltip = new TooltipExt();
        tooltip.setText(tipText);
        this.getSkinnable().setTooltip(tooltip);
    }

    /**
     * 获取组件所属窗口
     *
     * @return 组件
     */
    protected Window getWindow() {
        return this.getSkinnable().getScene().getWindow();
    }

    /**
     * 组件大小改变事件
     */
    protected void onSizeChanged() {
    }

    // @Override
    // protected final void layoutChildren(double x, double y, double w, double h) {
    //     super.layoutChildren(x, y, w, h);
    // }
}
