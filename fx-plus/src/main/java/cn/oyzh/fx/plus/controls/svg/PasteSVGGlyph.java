package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class PasteSVGGlyph extends SVGGlyph {

    public PasteSVGGlyph() {
        this.setUrl("/fx-plus/font/file-paste.svg");
    }

    public PasteSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(BaseResourceBundle.getBaseString("paste"));
        super.initNode();
    }
}
