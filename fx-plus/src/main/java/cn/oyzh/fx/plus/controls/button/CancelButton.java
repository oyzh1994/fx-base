package cn.oyzh.fx.plus.controls.button;


import cn.oyzh.fx.plus.controls.svg.CloseSVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * @author oyzh
 * @since 2020/10/29
 */
public class CancelButton extends IconButton {

    @Override
    public void initNode() {
        this.setPrefHeight(25);
        this.addClass("danger");
        this.setText(I18nHelper.cancel());
        this.setTipText(I18nHelper.cancel());
        this.init(new CloseSVGGlyph(), 0.7);
        this.setOnAction(actionEvent -> this.getScene().getWindow().hide());
        super.initNode();
    }
}
