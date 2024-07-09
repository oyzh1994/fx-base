package cn.oyzh.fx.plus.controls.enlarge;

import cn.oyzh.fx.plus.controls.area.FlexTextArea;
import cn.oyzh.fx.plus.controls.box.TextFiledHBox;
import cn.oyzh.fx.plus.controls.svg.EnlargeSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.textfield.ClearableTextField;
import cn.oyzh.fx.plus.i18n.I18nHelper;
import cn.oyzh.fx.plus.stage.StageExt;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import lombok.Getter;
import lombok.Setter;

/**
 * @author oyzh
 * @since 2024/07/09
 */
public class EnlargeTextFiledBox extends TextFiledHBox {

    /**
     * 弹窗
     */
    protected StageExt stage;

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
     * 显示弹窗组件
     */
    protected void onEnlargeButtonClick() {
        FlexTextArea textArea;
        if (this.stage == null) {
            textArea = new FlexTextArea();
            textArea.setText(this.getText());
            textArea.setFlexWidth("100%");
            textArea.setFlexHeight("100%");
            textArea.setPromptText(I18nHelper.pleaseInputContent());
            this.stage = new StageExt(this.getScene().getWindow(), textArea, this.enlargeWidth, this.enlargeHeight);
            this.stage.setTitleExt(I18nHelper.pleaseInputContent());
            this.stage.setOnCloseRequest(e -> this.setText(textArea.getTextTrim()));
        } else {
            textArea = (FlexTextArea) this.stage.root();
        }
        textArea.setText(this.getText());
        this.stage.show();
        this.stage.root().requestFocus();
    }

    public EnlargeTextFiledBox() {
        // 初始化按钮
        this.enlargeButton = new EnlargeSVGGlyph();
        this.enlargeButton.setSizeStr("13");
        this.enlargeButton.setTipText(I18nHelper.enlarge());
        this.enlargeButton.setEnableWaiting(false);
        this.enlargeButton.setFocusTraversable(false);
        this.enlargeButton.setOnMousePrimaryClicked(e -> this.onEnlargeButtonClick());
        this.enlargeButton.setOnMouseMoved(mouseEvent -> this.enlargeButton.setColor("#E36413"));
        this.enlargeButton.setOnMouseExited(mouseEvent -> this.enlargeButton.setColor(this.getButtonColor()));
        this.addChild(this.enlargeButton);

        // 设置文本输入框组件
        super.setTextField(new ClearableTextField());
    }

    @Override
    public void updateBounds() {
        super.updateBounds();
        this.updateChildren();
    }

    /**
     * 更新子节点
     */
    private void updateChildren() {
        double width = this.realWidth();
        double height = this.realHeight();
        double h1 = height - 10;
        double h2 = h1 / 2;
        this.enlargeButton.setSize(h2);
        this.textField.setRealHeight(h1);
        this.textField.setRealWidth(width - this.enlargeButton.getWidth() - 10);
        // 设置边距
        double t1 = (height - h1) / 2.0;
        double t2 = (height - h2) / 2.0;
        HBox.setMargin(this.textField, new Insets(t1, 0, 0, 3));
        HBox.setMargin(this.enlargeButton, new Insets(t2, 0, 0, -13));
    }
}
