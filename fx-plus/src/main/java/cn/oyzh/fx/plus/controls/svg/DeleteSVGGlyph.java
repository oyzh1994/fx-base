package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class DeleteSVGGlyph extends SVGGlyph {

    public DeleteSVGGlyph() {
        this.setUrl("/fx-plus/font/delete.svg");
    }

    public DeleteSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(BaseResourceBundle.getBaseString("delete"));
        super.initNode();
    }
}
