package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class PrevSVGGlyph extends SVGGlyph {

    public PrevSVGGlyph() {
        this.setUrl("/fx-plus/font/direction-up.svg");
    }

    public PrevSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(BaseResourceBundle.getBaseString("prev"));
        super.initNode();
    }
}
