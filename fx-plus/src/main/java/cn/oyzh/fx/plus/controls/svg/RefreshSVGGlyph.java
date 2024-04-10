package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class RefreshSVGGlyph extends SVGGlyph{

    @Override
    public void initNode() {
        this.setUrl("/fx-plus/font/reload.svg");
        this.setTipText(BaseResourceBundle.getBaseString("refresh"));
        super.initNode();
    }
}
