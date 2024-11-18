package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class TerminalSVGGlyph extends SVGGlyph {

    public TerminalSVGGlyph() {
        this.setUrl("/fx-plus/font/code library.svg");
    }

    public TerminalSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(I18nHelper.terminal());
        super.initNode();
    }
}
