package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class TerminalSVGGlyph extends SVGGlyph{

    @Override
    public void initNode() {
        this.setUrl("/fx-plus/font/code library.svg");
        this.setTipText(BaseResourceBundle.getBaseString("terminal"));
        super.initNode();
    }
}
