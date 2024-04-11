package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class RenameSVGGlyph extends SVGGlyph {

    public RenameSVGGlyph() {
        this.setUrl("/fx-plus/font/edit-square.svg");
    }

    public RenameSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(BaseResourceBundle.getBaseString("rename"));
        super.initNode();
    }
}
