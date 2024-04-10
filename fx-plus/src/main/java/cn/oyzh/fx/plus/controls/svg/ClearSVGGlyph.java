package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class ClearSVGGlyph extends SVGGlyph {

    @Override
    public void initNode() {
        this.setUrl("/fx-plus/font/clear2.svg");
        this.setTipText(BaseResourceBundle.getBaseString("clear"));
        super.initNode();
    }
}
