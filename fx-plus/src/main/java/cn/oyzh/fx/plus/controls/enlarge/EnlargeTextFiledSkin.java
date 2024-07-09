package cn.oyzh.fx.plus.controls.enlarge;

import cn.oyzh.fx.plus.controls.area.FlexTextArea;
import cn.oyzh.fx.plus.controls.svg.EnlargeSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;
import cn.oyzh.fx.plus.skin.ClearableTextFieldSkin;
import cn.oyzh.fx.plus.stage.StageExt;
import cn.oyzh.fx.plus.theme.ThemeManager;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.Setter;

/**
 * 展开文本输入框皮肤
 *
 * @author oyzh
 * @since 2024/07/09
 */
public class EnlargeTextFiledSkin extends ClearableTextFieldSkin {

    /**
     * 展开宽
     */
    @Getter
    @Setter
    protected double enlargeWidth = 480;

    /**
     * 展开高
     */
    @Getter
    @Setter
    protected double enlargeHeight = 300;

    /**
     * 展开按钮
     */
    protected final SVGGlyph enlargeButton;

    /**
     * 展开组件
     */
    protected StageExt stage;

    /**
     * 显示历史弹窗组件
     */
    protected void onEnlargeButtonClick() {
        if (this.stage == null) {
            FlexTextArea textArea = new FlexTextArea();
            textArea.setText(this.getText());
            textArea.setFlexWidth("100%");
            textArea.setFlexHeight("100%");
            textArea.setPromptText(I18nHelper.pleaseInputContent());
            this.stage = new StageExt(this.getWindow(), textArea, this.enlargeWidth, this.enlargeHeight);
            this.stage.setTitleExt(I18nHelper.pleaseInputContent());
            this.stage.setOnCloseRequest(e -> this.setText(textArea.getTextTrim()));
        }
        this.stage.show();
        this.stage.root().requestFocus();
    }

    public EnlargeTextFiledSkin(TextField textField) {
        super(textField);
        // 初始化历史按钮
        this.enlargeButton = new EnlargeSVGGlyph();
        this.enlargeButton.setSizeStr("13");
        this.enlargeButton.setTipText(I18nHelper.enlarge());
        this.enlargeButton.setEnableWaiting(false);
        this.enlargeButton.setFocusTraversable(false);
        this.enlargeButton.setPadding(new Insets(0));
        this.enlargeButton.setOnMousePrimaryClicked(e -> this.onEnlargeButtonClick());
        this.enlargeButton.setOnMouseMoved(mouseEvent -> this.enlargeButton.setColor("#E36413"));
        this.enlargeButton.setOnMouseExited(mouseEvent -> this.enlargeButton.setColor(this.getButtonColor()));
        this.getChildren().add(this.enlargeButton);
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
        this.enlargeButton.setSize(size);
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
        super.positionInArea(this.enlargeButton, 3, y * 0.9, w, h, btnSize, HPos.LEFT, VPos.CENTER);
    }
}
