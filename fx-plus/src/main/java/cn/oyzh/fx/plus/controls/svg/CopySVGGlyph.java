package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class CopySVGGlyph extends SVGGlyph {

    public CopySVGGlyph() {
        this.setUrl("/fx-plus/font/copy.svg");
    }

    public CopySVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(BaseResourceBundle.getBaseString("base.copy"));
        super.initNode();
    }
}
