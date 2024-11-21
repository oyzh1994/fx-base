package cn.oyzh.fx.gui.button;


import cn.oyzh.fx.gui.svg.glyph.FileSVGGlyph;
import cn.oyzh.fx.plus.controls.button.IconButton;
import cn.oyzh.i18n.I18nHelper;

/**
 *
 * @author oyzh
 * @since 2024/04/09
 */
public class FileButton extends IconButton {

    @Override
    public void initNode() {
        this.setPrefHeight(25);
        this.setText(I18nHelper.file());
        this.setTipText(I18nHelper.file());
        this.init(new FileSVGGlyph(), 0.7);
        super.initNode();
    }
}
