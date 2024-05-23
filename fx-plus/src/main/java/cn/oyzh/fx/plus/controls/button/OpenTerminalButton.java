package cn.oyzh.fx.plus.controls.button;


import cn.oyzh.fx.plus.controls.svg.TerminalSVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;

/**
 * 终端按钮
 *
 * @author oyzh
 * @since 2024/04/08
 */
public class OpenTerminalButton extends IconButton {

    @Override
    public void initNode() {
        this.setPrefHeight(25);
        this.addClass("default");
        this.setText(I18nHelper.openTerminal());
        this.setTipText(I18nHelper.openTerminal());
        this.init(new TerminalSVGGlyph(), 0.7);
        super.initNode();
    }
}
