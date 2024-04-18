package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class StopSVGGlyph extends SVGGlyph {

    public StopSVGGlyph() {
        this.setUrl("/fx-plus/font/stop-circle-line.svg");
    }

    public StopSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(I18nResourceBundle.i18nString("base.stop"));
        super.initNode();
    }
}
