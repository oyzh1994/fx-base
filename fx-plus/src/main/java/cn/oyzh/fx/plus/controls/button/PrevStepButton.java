package cn.oyzh.fx.plus.controls.button;


import cn.oyzh.fx.plus.gui.svg.glyph.PrevStepSVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/08/26
 */
public class PrevStepButton extends IconButton {

    @Override
    public void initNode() {
        this.setPrefHeight(25);
        this.addClass("accent");
        this.setText(I18nHelper.prevStep());
        this.setTipText(I18nHelper.prevStep());
        this.init(new PrevStepSVGGlyph(), 0.7);
        super.initNode();
    }
}
