package cn.oyzh.fx.plus.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class SaveSVGGlyph extends SVGGlyph {

    public SaveSVGGlyph() {
        this.setUrl("/fx-plus/font/save.svg");
    }

    public SaveSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(I18nHelper.save());
        super.initNode();
    }
}
