package cn.oyzh.fx.plus.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;

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
        this.setTipText(I18nHelper.paste());
        super.initNode();
    }
}
