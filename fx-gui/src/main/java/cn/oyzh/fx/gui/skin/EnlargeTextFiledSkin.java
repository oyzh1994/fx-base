package cn.oyzh.fx.gui.skin;

import cn.oyzh.fx.gui.svg.glyph.CancelSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.EnlargeSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.SubmitSVGGlyph;
import cn.oyzh.fx.plus.controls.box.FlexHBox;
import cn.oyzh.fx.plus.controls.box.FlexVBox;
import cn.oyzh.fx.plus.controls.text.area.FlexTextArea;
import cn.oyzh.fx.plus.skin.ActionTextFieldSkinExt;
import cn.oyzh.fx.plus.window.PopupExt;
import cn.oyzh.i18n.I18nHelper;
import javafx.geometry.Insets;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import lombok.Getter;
import lombok.Setter;

/**
 * 展开文本输入框皮肤
 *
 * @author oyzh
 * @since 2024/07/09
 */
public class EnlargeTextFiledSkin extends ActionTextFieldSkinExt {

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
     * 弹窗组件
     */
    protected PopupExt popup;

    @Override
    protected void onButtonClicked(MouseEvent e) {
        if (this.popup == null) {
            this.popup = new PopupExt();
            this.popup.setWidth(this.enlargeWidth);
            this.popup.setHeight(this.enlargeHeight);
            this.popup.setOnHiding(windowEvent -> this.handleHide());
        }
        // 输入框
        TextField textField = this.getSkinnable();
        textField.setDisable(true);
        // 文本节点
        FlexTextArea textArea = new FlexTextArea();
        textArea.setPromptText(I18nHelper.pleaseInputContent());
        textArea.setText(this.getText());
        // 按钮
        SubmitSVGGlyph ok = new SubmitSVGGlyph("13");
        ok.setOnMousePrimaryClicked(event -> this.onSubmit(textArea.getTextTrim()));
        CancelSVGGlyph cancel = new CancelSVGGlyph("12");
        cancel.setOnMousePrimaryClicked(event -> this.handleHide());
        HBox.setMargin(ok, new Insets(5, 0, 0, 5));
        HBox.setMargin(cancel, new Insets(5, 0, 0, 15));
        FlexHBox hBox = new FlexHBox(ok, cancel);
        // 组装阶段
        FlexVBox vBox = new FlexVBox(textArea, hBox);
        this.popup.content(vBox);
        this.popup.setOnHiding(event -> this.onSubmit(textArea.getTextTrim()));
        this.popup.showPopup(textField);
    }

    protected void handleHide() {
        this.popup.hide();
        this.getSkinnable().setDisable(false);
        this.resetButtonColor();
    }

    /**
     * 内容提交事件
     *
     * @param text 内容
     */
    protected void onSubmit(String text) {
        this.setText(text);
        this.handleHide();
    }

    public EnlargeTextFiledSkin(TextField textField) {
        super(textField, new EnlargeSVGGlyph("13"));
        this.button.disappear();
        this.button.setTipText(I18nHelper.enlarge());
    }

    @Override
    protected void updateButtonVisibility() {
        boolean visible = this.getSkinnable().isVisible();
        boolean disable = this.getSkinnable().isDisable();
        boolean hasFocus = this.getSkinnable().isFocused();
        boolean shouldBeVisible = !disable && visible && hasFocus;
        this.button.setVisible(shouldBeVisible);
    }
}
