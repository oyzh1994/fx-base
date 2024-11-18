package cn.oyzh.fx.gui.button;

import cn.oyzh.fx.plus.controls.button.IconButton;
import cn.oyzh.fx.gui.svg.glyph.TransportSVGGlyph;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class TransportButton extends IconButton {

    @Override
    public void initNode() {
        this.setPrefHeight(25);
        this.setText(I18nHelper.transport());
        this.setTipText(I18nHelper.transport());
        this.init(new TransportSVGGlyph(), 0.7);
        super.initNode();
    }
}
