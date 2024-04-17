package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class ExpendSVGGlyph extends SVGGlyph {

    public ExpendSVGGlyph() {
        this.setUrl("/fx-plus/font/arrow-to-right.svg");
    }

    public ExpendSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(BaseResourceBundle.getBaseString("base.expand"));
        super.initNode();
    }
}
