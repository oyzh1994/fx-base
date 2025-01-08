package cn.oyzh.fx.gui.button;


import cn.oyzh.fx.gui.svg.glyph.CalcSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.ClearSVGGlyph;
import cn.oyzh.fx.plus.controls.button.IconButton;
import cn.oyzh.i18n.I18nHelper;

/**
 * 重置按钮
 *
 * @author oyzh
 * @since 2024/04/08
 */
public class CalcButton extends IconButton {

    @Override
    public void initNode() {
        this.setPrefHeight(25);
        this.addClass("accent");
        this.setText(I18nHelper.calc());
        this.setTipText(I18nHelper.calc());
        this.init(new CalcSVGGlyph(), 0.7);
        super.initNode();
    }
}
