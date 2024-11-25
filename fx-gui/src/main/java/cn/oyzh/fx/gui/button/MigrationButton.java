package cn.oyzh.fx.gui.button;

import cn.oyzh.fx.gui.svg.glyph.MigrationSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.TransportSVGGlyph;
import cn.oyzh.fx.plus.controls.button.IconButton;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class MigrationButton extends IconButton {

    @Override
    public void initNode() {
        this.setPrefHeight(25);
        this.setText(I18nHelper.migration());
        this.setTipText(I18nHelper.migration());
        this.init(new MigrationSVGGlyph(), 0.7);
        super.initNode();
    }
}
