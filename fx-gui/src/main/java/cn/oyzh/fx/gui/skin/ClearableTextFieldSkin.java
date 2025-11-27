package cn.oyzh.fx.gui.skin;

import cn.oyzh.fx.gui.svg.glyph.CloseSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.node.NodeUtil;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

/**
 * 可清除文本输入框皮肤
 *
 * @author oyzh
 * @since 2023/10/9
 */
public class ClearableTextFieldSkin extends ActionTextFieldSkin {

    @Override
    protected void updateButtonVisibility() {
        boolean visible = this.getSkinnable().isVisible();
        boolean disable = this.getSkinnable().isDisable();
        boolean hasFocus = this.getSkinnable().isFocused();
        boolean isEmpty = this.getSkinnable().getText() == null || this.getSkinnable().getText().isEmpty();
        boolean shouldBeVisible = !disable && visible && hasFocus && !isEmpty;
        this.button.setVisible(shouldBeVisible);
    }

    public ClearableTextFieldSkin(TextField textField) {
        super(textField);
        // super(textField, new CloseSVGGlyph());
        // this.button.disappear();
        // this.button.setTipText(I18nHelper.clear());
    }

    @Override
    protected SVGGlyph getButton() {
        if (super.button == null) {
            this.button = new CloseSVGGlyph();
            super.initButton(this.button);
            double height = NodeUtil.getHeight(this.getSkinnable());
            double bthSize = height * 0.325;
            if (bthSize < 8) {
                bthSize = 8;
            } else if (bthSize > 12) {
                bthSize = 12;
            }
            this.button.setSize(bthSize);
        }
        return super.button;
    }

    @Override
    protected void onButtonClicked(MouseEvent e) {
        this.setText("");
    }
    //
    // @Override
    // protected double getButtonSizeMax() {
    //     return 12;
    // }
}
