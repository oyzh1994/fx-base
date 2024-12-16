package cn.oyzh.fx.gui.svg.glyph.titlebar;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/08/06
 */
public class TitleBarMaximumSVGGlyph extends SVGGlyph {

    public TitleBarMaximumSVGGlyph() {
        this.setUrl("/fx-svg/titlebar/maximum.svg");
    }

    public TitleBarMaximumSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
