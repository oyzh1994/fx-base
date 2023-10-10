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
public class NumberTextFieldSkin extends TextFieldSkin {

    /**
     * 文本输入框
     */
    protected TextField textField;

    /**
     * 增加按钮
     */
    protected SVGGlyph incrButton;

    /**
     * 减少按钮
     */
    protected SVGGlyph decrButton;

    /**
     * 文本监听器
     */
    private final InvalidationListener textChanged = observable -> this.updateButtonVisibility();

    /**
     * 焦点监听器
     */
    private final InvalidationListener focusChanged = observable -> this.updateButtonVisibility();

    /**
     * 修改按钮状态
     */
    private void updateButtonVisibility() {
        boolean hasFocus = this.textField.isFocused();
        boolean isEmpty = this.textField.getText() == null || this.textField.getText().isEmpty();
        boolean shouldBeVisible = hasFocus && !isEmpty;
        this.decrButton.setVisible(shouldBeVisible);
        this.incrButton.setVisible(shouldBeVisible);
    }

    public NumberTextFieldSkin(TextField textField, Runnable onIncr, Runnable onDecr) {
        super(textField);

        double h = textField.getHeight() / 2.d - 1;
        // 初始化增加、减少按钮
        this.incrButton = new SVGGlyph("/font/arrow-up-filling.svg");
        this.incrButton.setSize(h);
        this.incrButton.setVisible(false);
        this.incrButton.setColor("#000000");
        this.incrButton.managedBindVisible();
        this.incrButton.setEnableWaiting(false);
        this.incrButton.setFocusTraversable(false);
        this.incrButton.setPadding(new Insets(0));

        this.decrButton = new SVGGlyph("/font/arrow-down-filling.svg");
        this.decrButton.setSize(h);
        this.decrButton.setVisible(false);
        this.decrButton.setColor("#000000");
        this.decrButton.managedBindVisible();
        this.decrButton.setEnableWaiting(false);
        this.decrButton.setFocusTraversable(false);
        this.decrButton.setPadding(new Insets(0));

        if (onIncr != null) {
            this.incrButton.setOnMousePrimaryClicked(event -> onIncr.run());
        }
        if (onDecr != null) {
            this.decrButton.setOnMousePrimaryClicked(event -> onDecr.run());
        }

        // 添加到组件
        this.getChildren().addAll(this.incrButton, this.decrButton);

        // 初始化监听器
        this.textField = textField;
        this.textField.textProperty().addListener(this.textChanged);
        this.textField.focusedProperty().addListener(this.focusChanged);
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        super.layoutChildren(x, y, w, h);
        // 按钮大小，规则 (组件高/2-2)*0.9
        double size = (this.textField.getHeight() / 2.0 - 2) * 0.9;
        this.incrButton.setSize(size);
        this.decrButton.setSize(size);
        // 计算按钮实际大小
        double btnSize = this.snapSizeX(size);
        // 位移的areaX值，规则 组件宽+x-按钮实际大小
        double areaX = w + x - btnSize;
        // 位移的areaY1值，规则 组件高*0.1/2 +1
        double areaY1 = h * 0.1 / 2 + 1;
        // 位移的areaY2值，规则 组件高/2+areaY1+2
        double areaY2 = h / 2 + areaY1 + 2;
        // 设置按钮位置
        super.positionInArea(this.incrButton, areaX, areaY1, btnSize, btnSize, 0, HPos.CENTER, VPos.CENTER);
        super.positionInArea(this.decrButton, areaX, areaY2, btnSize, btnSize, 0, HPos.CENTER, VPos.CENTER);
    }

    @Override
    public void dispose() {
        // 清除监听器
        this.textField.textProperty().removeListener(this.textChanged);
        this.textField.focusedProperty().removeListener(this.focusChanged);
        super.dispose();
    }

    public void disableDecrButton() {
        this.decrButton.setDisable(true);
    }

    public void enableDecrButton() {
        this.decrButton.setDisable(false);
    }

    public void disableIncrButton() {
        this.incrButton.setDisable(false);
    }

    public void enableIncrButton() {
        this.incrButton.setDisable(false);
    }
}
