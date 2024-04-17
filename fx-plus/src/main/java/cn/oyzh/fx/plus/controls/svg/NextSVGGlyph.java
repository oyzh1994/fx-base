package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class NextSVGGlyph extends SVGGlyph {

    public NextSVGGlyph() {
        this.setUrl("/fx-plus/font/direction-down.svg");
    }

    public NextSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(BaseResourceBundle.getBaseString("base.next"));
        super.initNode();
    }
}
