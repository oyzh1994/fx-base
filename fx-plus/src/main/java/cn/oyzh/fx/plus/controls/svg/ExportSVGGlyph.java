package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class ExportSVGGlyph extends SVGGlyph {

    public ExportSVGGlyph() {
        this.setUrl("/fx-plus/font/export.svg");
    }

    public ExportSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(BaseResourceBundle.getBaseString("export"));
        super.initNode();
    }
}
