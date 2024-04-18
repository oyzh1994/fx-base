package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

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
        this.setTipText(I18nResourceBundle.i18nString("base.rename"));
        super.initNode();
    }
}
