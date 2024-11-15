package cn.oyzh.fx.plus.gui.button;


import cn.oyzh.fx.plus.controls.button.IconButton;
import cn.oyzh.fx.plus.gui.svg.glyph.CopySVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;

/**
 * 复制按钮
 *
 * @author oyzh
 * @since 2024/04/08
 */
public class CopyButton extends IconButton {

    @Override
    public void initNode() {
        this.setPrefHeight(25);
        this.setText(I18nHelper.copy());
        this.setTipText(I18nHelper.copy());
        this.init(new CopySVGGlyph(), 0.7);
        super.initNode();
    }
}
