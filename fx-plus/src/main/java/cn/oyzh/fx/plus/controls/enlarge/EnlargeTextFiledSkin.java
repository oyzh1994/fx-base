package cn.oyzh.fx.plus.controls.enlarge;

import cn.oyzh.fx.common.util.NumUtil;
import cn.oyzh.fx.plus.controls.area.FlexTextArea;
import cn.oyzh.fx.plus.controls.box.FlexVBox;
import cn.oyzh.fx.plus.controls.svg.EnlargeSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SaveSVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;
import cn.oyzh.fx.plus.skin.TextFieldSkinExt;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.window.PopupExt;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.Setter;

/**
 * 展开文本输入框皮肤
 *
 * @author oyzh
 * @since 2024/07/09
 */
public class EnlargeTextFiledSkin extends TextFieldSkinExt {

    /**
     * 展开宽
     */
    @Getter
    @Setter
    protected double enlargeWidth = 350;

    /**
     * 展开高
     */
    @Getter
    @Setter
    protected double enlargeHeight = 280;

    /**
     * 展开按钮
     */
    protected final SVGGlyph enlargeButton;

    /**
     * 弹窗组件
     */
    protected PopupExt popup;

    /**
     * 展开按钮点击事件
     */
    protected void onEnlargeButtonClick() {
        if (this.popup != null) {
            this.popup.hide();
        }
        TextField textField = this.getSkinnable();
        textField.setDisable(true);
        FlexTextArea textArea = new FlexTextArea();
        textArea.setPromptText(I18nHelper.pleaseInputContent());
        textArea.setText(this.getText());
        SaveSVGGlyph glyph = new SaveSVGGlyph("14");
        glyph.setOnMousePrimaryClicked(event -> this.onSubmit(textArea.getTextTrim()));
        FlexVBox vBox = new FlexVBox(textArea, glyph);
        VBox.setMargin(glyph, new Insets(5, 0, 0, 5));
        this.popup = new PopupExt();
        this.popup.setWidth(this.enlargeWidth);
        this.popup.setHeight(this.enlargeHeight);
        this.popup.content(vBox);
        this.popup.setOnHiding(event -> this.onSubmit(textArea.getTextTrim()));
        this.popup.showPopup(textField);
    }

    /**
     * 内容提交事件
     *
     * @param text 内容
     */
    protected void onSubmit(String text) {
        this.setText(text);
        this.popup.hide();
        this.getSkinnable().setDisable(false);
        this.enlargeButton.setColor(this.getButtonColor());
    }

    public EnlargeTextFiledSkin(TextField textField) {
        super(textField);
        // 初始化按钮
        this.enlargeButton = new EnlargeSVGGlyph("14");
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
        // 计算组件大小
        double btnSize = this.snapSizeX(h * 0.6);
        // 限制最大最小值
        btnSize = NumUtil.limit(btnSize, 14, 20);
        // 按钮大小，组件高度
        this.enlargeButton.setSize(btnSize);
        // 位移的areaX值，规则 组件宽+x-按钮大小
        double areaX = w + x - btnSize - 8;
        // 设置位置
        super.positionInArea(this.enlargeButton, areaX, y, btnSize, h, 0, HPos.CENTER, VPos.CENTER);
    }
}
