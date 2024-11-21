package cn.oyzh.fx.gui.button;


import cn.oyzh.fx.gui.svg.glyph.NextSVGGlyph;
import cn.oyzh.fx.plus.controls.button.IconButton;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/08/09
 */
public class NextButton extends IconButton {

    @Override
    public void initNode() {
        this.setPrefHeight(25);
        this.addClass("accent");
        this.setText(I18nHelper.next());
        this.setTipText(I18nHelper.next());
        this.init(new NextSVGGlyph(), 0.7);
        super.initNode();
    }
}
