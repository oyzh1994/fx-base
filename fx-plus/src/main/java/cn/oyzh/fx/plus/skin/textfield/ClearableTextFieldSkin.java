package cn.oyzh.fx.plus.skin.textfield;

import cn.oyzh.fx.plus.ext.TextFieldSkinExt;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.TextField;

/**
 * 可清除文本输入框皮肤
 *
 * @author oyzh
 * @since 2023/10/9
 */
public class ClearableTextFieldSkin extends TextFieldSkinExt {

    /**
     * 清除按钮
     */
    protected final SVGGlyph clearButton;

    /**
     * 修改按钮状态
     */
    protected void updateButtonVisibility() {
        boolean visible = this.getSkinnable().isVisible();
        boolean disable = this.getSkinnable().isDisable();
        boolean hasFocus = this.getSkinnable().isFocused();
        boolean isEmpty = this.getSkinnable().getText() == null || this.getSkinnable().getText().isEmpty();
        boolean shouldBeVisible = !disable && visible && hasFocus && !isEmpty;
        this.clearButton.setVisible(shouldBeVisible);
    }

    public ClearableTextFieldSkin(TextField textField) {
        super(textField);
        // 初始化清除按钮
        this.clearButton = new SVGGlyph("/fx-plus/font/clear.svg");
        this.clearButton.setTipText("清除");
        this.clearButton.setColor("#696969");
        this.clearButton.managedBindVisible();
        this.clearButton.setVisible(false);
        this.clearButton.setEnableWaiting(false);
        this.clearButton.setFocusTraversable(false);
        this.clearButton.setPadding(new Insets(0));
        this.clearButton.setOnMousePrimaryClicked(event -> this.getSkinnable().clear());
        this.clearButton.setOnMouseMoved(mouseEvent -> this.clearButton.setColor("#DC143C"));
        this.clearButton.setOnMouseExited(mouseEvent -> this.clearButton.setColor("#696969"));
        this.getChildren().add(this.clearButton);
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        super.layoutChildren(x, y, w, h);
        // 按钮大小，组件高度/2，最大12
        double size = Math.min(12, h / 2);
        this.clearButton.setSize(size);
        // 计算组件大小
        double btnSize = this.snapSizeX(size);
        // 位移的areaX值，规则 组件宽+x-按钮大小
        double areaX = w + x - btnSize;
        // 设置位置
        super.positionInArea(this.clearButton, areaX, y, btnSize, h, 0, HPos.CENTER, VPos.CENTER);
    }
}
