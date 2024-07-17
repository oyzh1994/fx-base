package cn.oyzh.fx.plus.controls.example;

import cn.oyzh.fx.common.util.NumUtil;
import cn.oyzh.fx.plus.controls.svg.ExampleSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;
import cn.oyzh.fx.plus.skin.ClearableTextFieldSkin;
import cn.oyzh.fx.plus.skin.TextFieldSkinExt;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.Setter;

/**
 * 示例文本输入框皮肤
 *
 * @author oyzh
 * @since 2024/07/04
 */
public class ExampleTextFieldSkin extends TextFieldSkinExt {

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
        // 计算组件大小
        double btnSize = this.snapSizeX(h * 0.7);
        // 限制最大最小值
        btnSize = NumUtil.limit(btnSize, 14, 20);
        // 按钮大小，组件高度
        this.exampleButton.setSize(btnSize);
        // 位移的areaX值，规则 组件宽+x-按钮大小
        double areaX = w + x - btnSize - 8;
        // 设置位置
        super.positionInArea(this.exampleButton, areaX, y, btnSize, h, 0, HPos.CENTER, VPos.CENTER);
    }
}
