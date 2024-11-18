package cn.oyzh.fx.gui.button;


import cn.oyzh.fx.plus.controls.button.IconButton;
import cn.oyzh.fx.gui.svg.glyph.NextStepSVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/08/26
 */
public class NextStepButton extends IconButton {

    @Override
    public void initNode() {
        this.setPrefHeight(25);
        this.addClass("accent");
        this.setText(I18nHelper.nextStep());
        this.setTipText(I18nHelper.nextStep());
        this.init(new NextStepSVGGlyph(), 0.7);
        super.initNode();
    }
}
