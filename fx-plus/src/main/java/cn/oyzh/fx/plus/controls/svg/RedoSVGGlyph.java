package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class RedoSVGGlyph extends SVGGlyph {

    public RedoSVGGlyph() {
        this.setUrl("/fx-plus/font/data_redo.svg");
    }

    public RedoSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(BaseResourceBundle.getBaseString("base.redo"));
        super.initNode();
    }
}
