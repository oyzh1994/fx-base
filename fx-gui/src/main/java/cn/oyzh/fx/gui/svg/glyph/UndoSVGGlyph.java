package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class UndoSVGGlyph extends SVGGlyph {

    public UndoSVGGlyph() {
        this.setUrl("/fx-gui/font/data_revoke.svg");
    }

    public UndoSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(I18nHelper.undo());
        super.initNode();
    }
}
