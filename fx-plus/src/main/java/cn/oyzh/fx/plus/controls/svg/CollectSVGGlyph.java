package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.I18nHelper;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class CollectSVGGlyph extends SVGGlyph {

    public CollectSVGGlyph() {
        this.setUrl("/fx-plus/font/star-l.svg");
    }

    public CollectSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(I18nHelper.collect());
        super.initNode();
    }
}
