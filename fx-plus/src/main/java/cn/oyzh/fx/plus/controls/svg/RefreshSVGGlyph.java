package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class RefreshSVGGlyph extends SVGGlyph {

    public RefreshSVGGlyph() {
        super("/fx-plus/font/reload.svg");
    }

    public RefreshSVGGlyph(String size) {
        super("/fx-plus/font/reload.svg", size);
    }

    @Override
    public void initNode() {
        this.setTipText(BaseResourceBundle.getBaseString("refresh"));
        super.initNode();
    }
}
