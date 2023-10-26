package cn.oyzh.fx.plus.ext;

import cn.oyzh.fx.plus.svg.SVGGlyph;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.TextField;

/**
 * @author oyzh
 * @since 2023/10/9
 */
public class NumberTextFieldSkin extends TextFieldSkinExt {

    /**
     * 增加按钮
     */
    protected SVGGlyph incrButton;

    /**
     * 减少按钮
     */
    protected SVGGlyph decrButton;

    @Override
    protected void updateButtonVisibility() {
        boolean visible = this.getSkinnable().isVisible();
        boolean disable = this.getSkinnable().isDisable();
        boolean hasFocus = this.getSkinnable().isFocused();
        boolean isEmpty = this.getSkinnable().getText() == null || this.getSkinnable().getText().isEmpty();
        boolean shouldBeVisible = !disable && visible && hasFocus && !isEmpty;
        this.decrButton.setVisible(shouldBeVisible);
        this.incrButton.setVisible(shouldBeVisible);
    }

    public NumberTextFieldSkin(TextField textField, Runnable onIncr, Runnable onDecr) {
        super(textField);
        double h = textField.getHeight() / 2.d - 1;
        // 初始化增加、减少按钮
        this.incrButton = new SVGGlyph("/fx-plus/font/arrow-up-filling.svg");
        this.incrButton.setSize(h);
        this.incrButton.setVisible(false);
        this.incrButton.setColor("#000000");
        this.incrButton.setTipText("增加值");
        this.incrButton.managedBindVisible();
        this.incrButton.setEnableWaiting(false);
        this.incrButton.setFocusTraversable(false);
        this.incrButton.setPadding(new Insets(0));

        this.decrButton = new SVGGlyph("/fx-plus/font/arrow-down-filling.svg");
        this.decrButton.setSize(h);
        this.decrButton.setVisible(false);
        this.decrButton.setColor("#000000");
        this.decrButton.setTipText("减少值");
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
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        super.layoutChildren(x, y, w, h);
        // 按钮大小，规则 (组件高/2-2)*0.9
        double size = (this.getSkinnable().getHeight() / 2.0 - 2) * 0.9;
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

    /**
     * 禁用减少值按钮
     */
    public void disableDecrButton() {
        this.decrButton.setDisable(true);
    }

    /**
     * 启用减少值按钮
     */
    public void enableDecrButton() {
        this.decrButton.setDisable(false);
    }

    /**
     * 禁用增加值按钮
     */
    public void disableIncrButton() {
        this.incrButton.setDisable(false);
    }

    /**
     * 启用增加值按钮
     */
    public void enableIncrButton() {
        this.incrButton.setDisable(false);
    }
}
