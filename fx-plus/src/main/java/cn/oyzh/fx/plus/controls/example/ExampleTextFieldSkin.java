package cn.oyzh.fx.plus.controls.example;

import cn.oyzh.fx.plus.controls.svg.ExampleSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;
import cn.oyzh.fx.plus.skin.ClearableTextFieldSkin;
import cn.oyzh.fx.plus.theme.ThemeManager;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.Setter;

/**
 * 文件文本输入框皮肤
 *
 * @author oyzh
 * @since 2024/0704
 */
public class ExampleTextFieldSkin extends ClearableTextFieldSkin {

    /**
     * 示例文本
     */
    @Getter
    @Setter
    protected String exampleText;

    /**
     * 示例按钮
     */
    protected final SVGGlyph exampleButton;

    /**
     * 显示历史弹窗组件
     */
    protected void onExampleButtonClick() {
        if (this.exampleText != null) {
            this.setText(this.exampleText);
        }
    }

    public ExampleTextFieldSkin(TextField textField) {
        super(textField);
        // 初始化历史按钮
        this.exampleButton = new ExampleSVGGlyph();
        this.exampleButton.setTipText(I18nHelper.example());
        this.exampleButton.setEnableWaiting(false);
        this.exampleButton.setFocusTraversable(false);
        this.exampleButton.setPadding(new Insets(0));
        this.exampleButton.setOnMousePrimaryClicked(e -> this.onExampleButtonClick());
        this.exampleButton.setOnMouseMoved(mouseEvent -> this.exampleButton.setColor("#E36413"));
        this.exampleButton.setOnMouseExited(mouseEvent -> this.exampleButton.setColor(this.getButtonColor()));
        this.getChildren().add(this.exampleButton);
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
        this.exampleButton.setSize(size);
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
        super.positionInArea(this.exampleButton, 3, y * 0.9, w, h, btnSize, HPos.LEFT, VPos.CENTER);
    }
}
