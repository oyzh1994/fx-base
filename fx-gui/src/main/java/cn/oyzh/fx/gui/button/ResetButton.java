package cn.oyzh.fx.gui.button;


import cn.oyzh.fx.plus.controls.button.IconButton;
import cn.oyzh.fx.gui.svg.glyph.ResetSVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;

/**
 * 重置按钮
 *
 * @author oyzh
 * @since 2024/04/08
 */
public class ResetButton extends IconButton {

    @Override
    public void initNode() {
        this.setPrefHeight(25);
        this.setText(I18nHelper.reset());
        this.setTipText(I18nHelper.reset());
        this.init(new ResetSVGGlyph(), 0.7);
        super.initNode();
    }
}
