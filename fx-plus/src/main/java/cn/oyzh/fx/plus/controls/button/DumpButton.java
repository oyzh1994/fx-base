package cn.oyzh.fx.plus.controls.button;

import cn.oyzh.fx.plus.gui.svg.glyph.database.DumpSVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/08/28
 */
public class DumpButton extends IconButton{

    @Override
    public void initNode() {
        this.setPrefHeight(25);
        this.setText(I18nHelper.dump());
        this.setTipText(I18nHelper.dump());
        this.init(new DumpSVGGlyph(), 0.7);
        super.initNode();
    }
}
