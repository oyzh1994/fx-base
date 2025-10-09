package cn.oyzh.fx.gui.button;


import cn.oyzh.fx.gui.svg.glyph.TerminalSVGGlyph;
import cn.oyzh.fx.plus.controls.button.IconButton;
import cn.oyzh.i18n.I18nHelper;

/**
 * 本地终端按钮
 *
 * @author oyzh
 * @since 2024/04/08
 */
public class LocalTerminalButton extends IconButton {

    @Override
    public void initNode() {
        this.setRealHeight(30);
        this.addClass("default");
        this.setText(I18nHelper.localTerminal());
        this.setTipText(I18nHelper.localTerminal());
        this.init(new TerminalSVGGlyph(), 0.7);
        super.initNode();
    }
}
