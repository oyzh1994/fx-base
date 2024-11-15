package cn.oyzh.fx.plus.controls.button;


import cn.oyzh.fx.plus.gui.svg.glyph.ClearSVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;

/**
 * 重置按钮
 *
 * @author oyzh
 * @since 2024/04/08
 */
public class ClearButton extends IconButton {

    @Override
    public void initNode() {
        this.setPrefHeight(25);
        this.addClass("danger");
        this.setText(I18nHelper.clear());
        this.setTipText(I18nHelper.clear());
        this.init(new ClearSVGGlyph(), 0.7);
        super.initNode();
    }
}
