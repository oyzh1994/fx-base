package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class AddFillSVGGlyph extends SVGGlyph {

    public AddFillSVGGlyph() {
        super("/fx-plus/font/add-fill.svg");
    }

    public AddFillSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(BaseResourceBundle.getBaseString("add"));
        super.initNode();
    }
}
