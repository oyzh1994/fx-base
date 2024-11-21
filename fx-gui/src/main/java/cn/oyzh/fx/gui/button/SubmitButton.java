package cn.oyzh.fx.gui.button;


import cn.oyzh.fx.gui.svg.glyph.SubmitSVGGlyph;
import cn.oyzh.fx.plus.controls.button.IconButton;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2020/10/29
 */
public class SubmitButton extends IconButton {

    @Override
    public void initNode() {
        this.setPrefHeight(25);
        this.addClass("accent");
        this.setText(I18nHelper.submit());
        this.setTipText(I18nHelper.submit());
        this.init(new SubmitSVGGlyph(), 0.7);
        super.initNode();
    }
}
