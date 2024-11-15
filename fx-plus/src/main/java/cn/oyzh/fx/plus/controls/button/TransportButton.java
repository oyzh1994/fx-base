package cn.oyzh.fx.plus.controls.button;

import cn.oyzh.fx.plus.gui.svg.glyph.TransportSVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class TransportButton extends IconButton{

    @Override
    public void initNode() {
        this.setPrefHeight(25);
        this.setText(I18nHelper.transport());
        this.setTipText(I18nHelper.transport());
        this.init(new TransportSVGGlyph(), 0.7);
        super.initNode();
    }
}
