package cn.oyzh.fx.plus.controls.button;


import cn.oyzh.fx.plus.controls.svg.FileSVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

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
