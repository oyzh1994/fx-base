package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class NextSVGGlyph extends SVGGlyph {

    @Override
    public void initNode() {
        this.setUrl("/fx-plus/font/direction-down.svg");
        this.setTipText(BaseResourceBundle.getBaseString("next"));
        super.initNode();
    }
}
