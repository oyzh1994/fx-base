package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class ImportSVGGlyph extends SVGGlyph {

    public ImportSVGGlyph() {
        this.setUrl("/fx-plus/font/Import.svg");
    }

    public ImportSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(BaseResourceBundle.getBaseString("base.import"));
        super.initNode();
    }
}
