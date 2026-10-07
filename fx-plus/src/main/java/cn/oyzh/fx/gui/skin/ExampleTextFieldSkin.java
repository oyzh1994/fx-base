package cn.oyzh.fx.gui.skin;

import cn.oyzh.fx.gui.svg.glyph.ExampleSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

/**
 * 示例文本输入框皮肤
 *
 * @author oyzh
 * @since 2024/07/04
 */
public class ExampleTextFieldSkin extends ActionTextFieldSkin {

    /**
     * 获取示例文本
     *
     * @return 示例文本
     */
    public String getExampleText() {
        return exampleText;
    }

    /**
     * 设置示例文本
     *
     * @param exampleText 示例文本
     */
    public void setExampleText(String exampleText) {
        this.exampleText = exampleText;
    }

    /**
     * 示例文本
     */
    protected String exampleText;

    /**
     * 按钮点击时将示例文本填入输入框
     *
     * @param e 鼠标事件
     */
    protected void onButtonClicked(MouseEvent e) {
        if (this.exampleText != null) {
            this.setText(this.exampleText);
        }
    }

    /**
     * 构造示例文本字段皮肤对象。
     *
     * @param textField 文本框
     */
    public ExampleTextFieldSkin(TextField textField) {
        super(textField);
    }

    @Override
    protected SVGGlyph getButton() {
        if (super.button == null) {
            super.button = new ExampleSVGGlyph();
            super.initButton(super.button);
        }
        return super.button;
    }

    @Override
    protected void updateButtonVisibility() {
        boolean visible = this.getSkinnable().isVisible();
        boolean disable = this.getSkinnable().isDisable();
        boolean hasFocus = this.getSkinnable().isFocused();
        boolean shouldBeVisible = !disable && visible && hasFocus;
        this.button.setVisible(shouldBeVisible);
    }

    @Override
    public void dispose() {
        this.exampleText = null;
        super.dispose();
    }
}
