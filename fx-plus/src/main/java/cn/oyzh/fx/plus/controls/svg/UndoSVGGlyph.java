package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class UndoSVGGlyph extends SVGGlyph {

    public UndoSVGGlyph() {
        this.setUrl("/fx-plus/font/data_revoke.svg");
    }

    public UndoSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(BaseResourceBundle.getBaseString("base.undo"));
        super.initNode();
    }
}
