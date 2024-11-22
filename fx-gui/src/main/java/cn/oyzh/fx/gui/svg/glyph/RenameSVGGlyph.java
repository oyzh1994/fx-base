package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class RenameSVGGlyph extends SVGGlyph {

    public RenameSVGGlyph() {
        this.setUrl("/fx-gui/font/edit-square.svg");
    }

    public RenameSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(I18nHelper.rename());
        super.initNode();
    }
}
