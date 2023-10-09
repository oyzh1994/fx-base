package cn.oyzh.fx.plus.ext;

import cn.oyzh.fx.plus.svg.SVGGlyph;
import javafx.beans.InvalidationListener;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.TextFieldSkin;

/**
 * @author oyzh
 * @since 2023/10/9
 */
public class ClearableTextFieldSkin extends TextFieldSkin {

    /**
     * 文本输入框
     */
    protected TextField textField;

    /**
     * 清除按钮
     */
    protected final SVGGlyph clearButton;

    /**
     * 监听器
     */
    private final InvalidationListener textChanged = observable -> this.updateButtonVisibility();

    /**
     * 监听器
     */
    private final InvalidationListener focusChanged = observable -> this.updateButtonVisibility();

    /**
     * 修改按钮状态
     */
    private void updateButtonVisibility() {
        boolean hasFocus = this.textField.isFocused();
        boolean isEmpty = this.textField.getText() == null || this.textField.getText().isEmpty();
        boolean shouldBeVisible = hasFocus && !isEmpty;
        this.clearButton.setVisible(shouldBeVisible);
    }

    public ClearableTextFieldSkin(TextField textField) {
        super(textField);

        // 初始化清除按钮
        this.clearButton = new SVGGlyph("/font/clear.svg");
        this.clearButton.setSize(8);
        this.clearButton.setEnableWaiting(false);
        this.clearButton.setPadding(new Insets(0));
        this.clearButton.setColor("#696969");
        this.clearButton.setFocusTraversable(false);
        this.clearButton.setOnMousePrimaryClicked(event -> this.textField.clear());
        this.clearButton.setOnMouseMoved(mouseEvent -> this.clearButton.setColor("#DC143C"));
        this.clearButton.setOnMouseExited(mouseEvent -> this.clearButton.setColor("#696969"));
        this.clearButton.setVisible(false);
        this.clearButton.managedBindVisible();
        this.getChildren().add(this.clearButton);

        // 初始化监听器
        this.textField = textField;
        this.textField.textProperty().addListener(this.textChanged);
        this.textField.focusedProperty().addListener(this.focusChanged);
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        super.layoutChildren(x, y, w, h);
        final double clearButtonWidth = snapSizeX(this.clearButton.getWidth());
        super.positionInArea(this.clearButton, (x + w) - clearButtonWidth, y, clearButtonWidth, h, 0, HPos.CENTER, VPos.CENTER);
    }

    @Override
    public void dispose() {
        // 清除监听器
        this.textField.textProperty().removeListener(this.textChanged);
        this.textField.focusedProperty().removeListener(this.focusChanged);
        super.dispose();
    }
}
