package cn.oyzh.fx.plus.skin;

import cn.oyzh.fx.plus.information.TooltipExt;
import cn.oyzh.fx.plus.theme.ThemeManager;
import javafx.beans.InvalidationListener;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.TextFieldSkin;
import javafx.scene.paint.Color;
import javafx.stage.Window;

/**
 * 文本域皮肤扩展
 *
 * @author oyzh
 * @since 2023/10/25
 */
public class TextFieldSkinExt extends TextFieldSkin {

    /**
     * 可见监听器
     */
    protected final InvalidationListener visibilityChanged = observable -> this.updateButtonVisibility();

    /**
     * 修改按钮状态
     */
    protected void updateButtonVisibility() {
    }

    public TextFieldSkinExt(TextField control) {
        super(control);
        // 初始化监听器
        control.textProperty().addListener(this.visibilityChanged);
        control.focusedProperty().addListener(this.visibilityChanged);
        control.visibleProperty().addListener(this.visibilityChanged);
        control.disableProperty().addListener(this.visibilityChanged);
    }

    @Override
    public void dispose() {
        // 清除监听器
        this.getSkinnable().textProperty().removeListener(this.visibilityChanged);
        this.getSkinnable().focusedProperty().removeListener(this.visibilityChanged);
        this.getSkinnable().visibleProperty().removeListener(this.visibilityChanged);
        this.getSkinnable().disableProperty().removeListener(this.visibilityChanged);
        this.getSkinnable().setOnMouseExited(null);
        this.getSkinnable().setOnMouseEntered(null);
        super.dispose();
    }

    /**
     * 获取按钮颜色
     *
     * @return 按钮颜色
     */
    protected Color getButtonColor() {
        if (!ThemeManager.isDarkMode()) {
            return Color.BLACK;
        }
        return Color.WHITE;
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

    protected Window getWindow() {
        return this.getSkinnable().getScene().getWindow();
    }
}
