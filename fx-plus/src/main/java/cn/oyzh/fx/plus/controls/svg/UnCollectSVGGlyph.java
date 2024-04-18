package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class UnCollectSVGGlyph extends SVGGlyph {

    public UnCollectSVGGlyph() {
        this.setUrl("/fx-plus/font/star.svg");
    }

    public UnCollectSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(I18nResourceBundle.i18nString("base.unCollect"));
        super.initNode();
    }
}
