package cn.oyzh.fx.gui.button;


import cn.oyzh.fx.gui.svg.glyph.CloseSVGGlyph;
import cn.oyzh.fx.plus.controls.button.IconButton;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2020/10/29
 */
public class CancelButton extends IconButton {

    @Override
    public void initNode() {
        this.setRealHeight(30);
        this.addClass("danger");
        this.setText(I18nHelper.cancel());
        this.setTipText(I18nHelper.cancel());
        this.init(new CloseSVGGlyph(), 0.7);
        this.setOnAction(actionEvent -> this.getScene().getWindow().hide());
        super.initNode();
    }
}
