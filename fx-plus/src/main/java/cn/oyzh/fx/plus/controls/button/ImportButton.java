package cn.oyzh.fx.plus.controls.button;

import cn.oyzh.fx.plus.gui.svg.glyph.ImportSVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class ImportButton extends IconButton{

    @Override
    public void initNode() {
        this.setPrefHeight(25);
        this.setText(I18nHelper._import());
        this.setTipText(I18nHelper._import());
        this.init(new ImportSVGGlyph(), 0.7);
        super.initNode();
    }
}
